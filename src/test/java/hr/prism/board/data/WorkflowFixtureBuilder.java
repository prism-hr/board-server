package hr.prism.board.data;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.enums.State.*;

/**
 * Convenience class go generate SQL fixtures to support testing the workflow.
 * Not used during normal test operation - to get the fixtures run a test and debug the return from makeInserts.
 */

@Service
public class WorkflowFixtureBuilder {

    private final ResourceService resourceService;

    public WorkflowFixtureBuilder(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostConstruct
    public Map<String, String> makeInserts() {
        Map<String, String> inserts = new HashMap<>();

        inserts.put("ACTION",
            makeResourceInserts(
                new State[]{ACCEPTED, REJECTED},
                new State[]{ACCEPTED, REJECTED},
                new State[]{ACCEPTED}));

        inserts.put("DEPARTMENT", makeResourceInserts(
            new State[]{DRAFT, PENDING, ACCEPTED, REJECTED},
            new State[]{ACCEPTED},
            new State[]{ACCEPTED}));

        inserts.put("BOARD", makeResourceInserts(
            new State[]{ACCEPTED, REJECTED},
            new State[]{ACCEPTED, REJECTED},
            new State[]{ACCEPTED}));

        inserts.put("POST", makeResourceInserts(
            new State[]{ACCEPTED, REJECTED},
            new State[]{ACCEPTED, REJECTED},
            new State[]{DRAFT, PENDING, ACCEPTED, EXPIRED, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED}));

        return inserts;
    }

    private String makeResourceInserts(State[] departmentStates, State[] boardStates, State[] postStates) {
        LocalDateTime baseline = LocalDateTime.now();
        List<Resource> resources = new ArrayList<>();

        University university = setUpUniversity(baseline);
        resources.add(university);
        for (State departmentState : departmentStates) {
            resources.addAll(setUpDepartment(baseline, university, departmentState, boardStates, postStates));
        }

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            resource.setId((long) (i + 1));
            resourceService.setIndexDataAndQuarter(resource);

            Scope resourceScope = resource.getScope();
            State resourceState = resource.getState();
            rows.add(
                "(" +
                    resource.getId() + ", " +
                    "'" + resourceScope + "', " +
                    resource.getParent().getId() + ", " +
                    "'" + resource.getName() + "', " +
                    "'" + (resourceScope == POST ?
                    resource.getParent().getHandle() + "/post-" + resource.getState().name().toLowerCase() :
                    resource.getHandle()) + "', " +
                    (resourceScope == POST && resourceState == EXPIRED ? "NOW() - INTERVAL 1 DAY" : "NULL") + ", " +
                    "'" + resource.getState() + "', " +
                    "'" + resource.getPreviousState() + "', " +
                    "'" + resource.getIndexData() + "', " +
                    "'" + resource.getQuarter() + "', " +
                    "'" + Timestamp.valueOf(resource.getCreatedTimestamp()) + "'" +
                    ")");
        }

        List<String> userInserts = makeUserInserts(baseline, resources);
        List<String> inserts = ImmutableList.of(
            "INSERT INTO resource (id, scope, parent_id, name, handle, dead_timestamp, state, previous_state, " +
                "index_data, quarter, created_timestamp)\n" +
                "VALUES \n\t" + Joiner.on(",\n\t").join(rows) + ";",
            makeResourceRelationInsert(baseline, resources),
            userInserts.get(0),
            userInserts.get(1));

        return Joiner.on("\n\n").join(inserts);
    }

    private String makeResourceRelationInsert(LocalDateTime baseline, List<Resource> resources) {
        List<ResourceRelation> resourceRelations = new ArrayList<>();
        for (Resource resource : resources) {
            Resource parent = resource;
            while (true) {
                resourceRelations.add(setUpResourceRelation(baseline, parent, resource));

                Resource grandParent = parent.getParent();
                if (parent.equals(grandParent)) {
                    break;
                }

                parent = grandParent;
            }
        }

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < resourceRelations.size(); i++) {
            ResourceRelation resourceRelation = resourceRelations.get(i);
            resourceRelation.setId((long) +(i + 1));

            rows.add(
                "(" +
                    resourceRelation.getId() + ", " +
                    resourceRelation.getResource1().getId() + ", " +
                    resourceRelation.getResource2().getId() + ", " +
                    "'" + Timestamp.valueOf(resourceRelation.getCreatedTimestamp()) + "'" +
                    ")"
            );
        }

        return "INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp)\n" +
            "VALUES \n\t" + Joiner.on(",\n\t").join(rows) + ";";
    }

    private List<String> makeUserInserts(LocalDateTime baseline, List<Resource> resources) {
        User departmentAdministrator = setUpUser(baseline, "department-administrator");
        User departmentAuthor = setUpUser(baseline, "department-author");
        User pendingDepartmentMember = setUpUser(baseline, "department-member-pending");
        User acceptedDepartmentMember = setUpUser(baseline, "department-member-accepted");
        User rejectedDepartmentMember = setUpUser(baseline, "department-member-rejected");

        List<User> users = new ArrayList<>(
            ImmutableList.of(
                departmentAdministrator,
                departmentAuthor,
                pendingDepartmentMember,
                acceptedDepartmentMember,
                rejectedDepartmentMember));

        List<UserRole> userRoles = new ArrayList<>();
        Map<Resource, User> departmentPostAdministrators = new HashMap<>();
        for (Resource resource : resources) {
            String name = resource.getName();
            switch (resource.getScope()) {
                case DEPARTMENT:
                    userRoles.add(setUpUserRole(baseline, resource, departmentAdministrator, ADMINISTRATOR, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, departmentAuthor, AUTHOR, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, pendingDepartmentMember, MEMBER, PENDING));
                    userRoles.add(setUpUserRole(baseline, resource, acceptedDepartmentMember, MEMBER, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, rejectedDepartmentMember, MEMBER, REJECTED));

                    User thisDepartmentAdministrator = setUpUser(baseline, name + "-administrator");
                    User thisDepartmentAuthor = setUpUser(baseline, name + "-author");
                    User thisPendingDepartmentMember = setUpUser(baseline, name + "-member-pending");
                    User thisAcceptedDepartmentMember = setUpUser(baseline, name + "-member-accepted");
                    User thisRejectedDepartmentMember = setUpUser(baseline, name + "-member-rejected");
                    users.addAll(
                        ImmutableList.of(
                            thisDepartmentAdministrator,
                            thisDepartmentAuthor,
                            thisPendingDepartmentMember,
                            thisAcceptedDepartmentMember,
                            thisRejectedDepartmentMember));

                    userRoles.add(
                        setUpUserRole(baseline, resource, thisDepartmentAdministrator, ADMINISTRATOR, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, thisDepartmentAuthor, AUTHOR, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, thisPendingDepartmentMember, MEMBER, PENDING));
                    userRoles.add(setUpUserRole(baseline, resource, thisAcceptedDepartmentMember, MEMBER, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, thisRejectedDepartmentMember, MEMBER, REJECTED));
                    continue;
                case POST:
                    Resource department = resource.getParent().getParent();
                    User thisPostAdministrator = departmentPostAdministrators.get(department);
                    if (thisPostAdministrator == null) {
                        thisPostAdministrator =
                            setUpUser(baseline, department.getName() + "-post-administrator");
                        users.add(thisPostAdministrator);
                        departmentPostAdministrators.put(department, thisPostAdministrator);
                    }

                    userRoles.add(setUpUserRole(baseline, resource, thisPostAdministrator, ADMINISTRATOR, ACCEPTED));
            }
        }

        users.add(setUpUser(baseline, "no-roles"));

        List<String> userRows = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            user.setId((long) (i + 1));

            userRows.add(
                "(" +
                    user.getId() + ", " +
                    "UUID(), " +
                    "'" + user.getGivenName() + "', " +
                    "'" + user.getSurname() + "', " +
                    "'" + user.getEmail() + "', " +
                    "'" + user.getEmailDisplay() + "', " +
                    "'" + Timestamp.valueOf(user.getCreatedTimestamp()) + "'" +
                    ")");
        }

        List<String> userRoleRows = new ArrayList<>();
        for (int i = 0; i < userRoles.size(); i++) {
            UserRole userRole = userRoles.get(i);
            userRole.setId((long) (i + 1));

            userRoleRows.add(
                "(" +
                    userRole.getId() + ", " +
                    "UUID(), " +
                    userRole.getResource().getId() + ", " +
                    userRole.getUser().getId() + ", " +
                    "'" + userRole.getRole() + "', " +
                    "'" + userRole.getState() + "', " +
                    "'" + Timestamp.valueOf(userRole.getCreatedTimestamp()) + "'" +
                    ")");
        }

        return ImmutableList.of(
            "INSERT INTO user (id, uuid, given_name, surname, email, email_display, created_timestamp)\n" +
                "VALUES\n\t" + Joiner.on(",\n\t").join(userRows) + ";",
            "INSERT INTO user_role (id, uuid, resource_id, user_id, role, state, created_timestamp)\n" +
                "VALUES\n\t" + Joiner.on(",\n\t").join(userRoleRows) + ";");
    }

    private University setUpUniversity(LocalDateTime baseline) {
        University university = new University();
        university.setParent(university);
        university.setName("university");
        university.setHandle("university");
        university.setState(ACCEPTED);
        university.setPreviousState(ACCEPTED);
        university.setCreatedTimestamp(baseline);
        return university;
    }

    private List<Resource> setUpDepartment(LocalDateTime baseline, University university, State state,
                                           State[] boardStates, State[] postStates) {
        Department department = new Department();
        department.setParent(university);

        List<Resource> resources = new ArrayList<>();
        resources.add(department);

        department.setName("department-" + state.name().toLowerCase());
        department.setHandle(university.getHandle() + "/" + department.getName());

        department.setState(state);
        department.setPreviousState(state);
        department.setCreatedTimestamp(baseline);

        for (State boardState : boardStates) {
            resources.addAll(setUpBoard(baseline, department, boardState, postStates));
        }

        return resources;
    }

    private List<Resource> setUpBoard(LocalDateTime baseline, Department department, State state, State[] postStates) {
        Board board = new Board();
        board.setParent(department);

        List<Resource> resources = new ArrayList<>();
        resources.add(board);

        String boardName = "board-" + state.name().toLowerCase();
        board.setName(department.getName() + "-" + boardName);
        board.setHandle(department.getHandle() + "/" + boardName);

        board.setState(state);
        board.setPreviousState(state);
        board.setCreatedTimestamp(baseline);

        for (State postState : postStates) {
            resources.add(setUpPost(baseline, board, postState));
        }

        return resources;
    }

    private Resource setUpPost(LocalDateTime baseline, Board board, State state) {
        Post post = new Post();
        post.setParent(board);
        post.setName(board.getName() + "-" + "post-" + state.name().toLowerCase());
        post.setState(state);
        post.setPreviousState(state);
        post.setCreatedTimestamp(baseline);
        return post;
    }

    private ResourceRelation setUpResourceRelation(LocalDateTime baseline, Resource resource1, Resource resource2) {
        ResourceRelation resourceRelation = new ResourceRelation();
        resourceRelation.setResource1(resource1);
        resourceRelation.setResource2(resource2);
        resourceRelation.setCreatedTimestamp(baseline);
        return resourceRelation;
    }


    private User setUpUser(LocalDateTime baseline, String name) {
        User user = new User();
        user.setGivenName(name);
        user.setSurname(name);
        user.setEmail(name + "@prism.hr");
        user.setCreatedTimestamp(baseline);
        return user;
    }

    private UserRole setUpUserRole(LocalDateTime baseline, Resource resource, User user, Role role, State state) {
        UserRole userRole = new UserRole();
        userRole.setResource(resource);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setState(state);
        userRole.setCreatedTimestamp(baseline);
        return userRole;
    }

}
