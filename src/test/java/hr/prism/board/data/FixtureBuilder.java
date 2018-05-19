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
import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.*;

@Service
public class FixtureBuilder {

    private final ResourceService resourceService;

    public FixtureBuilder(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostConstruct
    public Map<Scope, String> makeInserts() {
        Map<Scope, String> inserts = new HashMap<>();

        inserts.put(DEPARTMENT, makeResourceInserts(
            DEPARTMENT,
            new State[]{ACCEPTED},
            new State[]{ACCEPTED}));

        inserts.put(BOARD, makeResourceInserts(
            BOARD,
            new State[]{ACCEPTED, REJECTED},
            new State[]{ACCEPTED}));

        inserts.put(POST, makeResourceInserts(
            POST,
            new State[]{ACCEPTED, REJECTED},
            new State[]{DRAFT, PENDING, ACCEPTED, EXPIRED, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED}));

        return inserts;
    }

    private String makeResourceInserts(Scope scope, State[] boardStates, State[] postStates) {
        LocalDateTime baseline = LocalDateTime.now();
        List<Resource> resources = new ArrayList<>();

        University university = setUpUniversity(baseline);
        resources.add(university);
        resources.addAll(setUpDepartment(baseline, university, ACCEPTED, boardStates, postStates));
        resources.addAll(setUpDepartment(baseline, university, REJECTED, boardStates, postStates));

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            resource.setId((long) (i + 1));
            resourceService.setIndexDataAndQuarter(resource);

            Scope resourceScope = resource.getScope();
            rows.add(
                "(" +
                    resource.getId() + ", " +
                    "'" + resourceScope + "', " +
                    resource.getParent().getId() + ", " +
                    "'" + resource.getName() + "', " +
                    (resourceScope == POST ? "NULL, " : "'" + resource.getHandle() + "', ") +
                    "'" + resource.getState() + "', " +
                    "'" + resource.getPreviousState() + "', " +
                    "'" + resource.getIndexData() + "', " +
                    "'" + resource.getQuarter() + "', " +
                    "'" + Timestamp.valueOf(resource.getCreatedTimestamp()) + "'" +
                    ")");
        }

        List<String> userInserts = makeUserInserts(baseline, scope, resources);
        List<String> inserts = ImmutableList.of(
            "INSERT INTO resource (id, scope, parent_id, name, handle, state, previous_state, index_data, quarter, " +
                "created_timestamp)\n" +
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

    private List<String> makeUserInserts(LocalDateTime baseline, Scope scope, List<Resource> resources) {
        User departmentAdministrator = setUpUser(baseline, "department-administrator");
        User otherDepartmentAdministrator = setUpUser(baseline, "other-department-administrator");

        User departmentAuthor = setUpUser(baseline, "department-author");
        User otherDepartmentAuthor = setUpUser(baseline, "other-department-author");

        User acceptedDepartmentMember = setUpUser(baseline, "accepted-department-member");
        User otherAcceptedDepartmentMember = setUpUser(baseline, "other-accepted-department-member");

        User pendingDepartmentMember = null;
        User otherPendingDepartmentMember = null;

        User rejectedDepartmentMember = null;
        User otherRejectedDepartmentMember = null;

        if (scope == POST) {
            pendingDepartmentMember = setUpUser(baseline, "pending-department-member");
            otherPendingDepartmentMember = setUpUser(baseline, "other-pending-department-member");

            rejectedDepartmentMember = setUpUser(baseline, "rejected-department-member");
            otherRejectedDepartmentMember = setUpUser(baseline, "other-rejected-department-member");
        }

        User postAdministrator = setUpUser(baseline, "post-administrator");
        User otherPostAdministrator = setUpUser(baseline, "other-post-administrator");

        User unprivileged = setUpUser(baseline, "unprivileged");

        List<User> users = ImmutableList.of(
            departmentAdministrator,
            otherDepartmentAdministrator,
            departmentAuthor,
            otherDepartmentAuthor,
            acceptedDepartmentMember,
            otherAcceptedDepartmentMember,
            postAdministrator,
            otherPostAdministrator,
            unprivileged);
        if (scope == POST) {
            users = ImmutableList.of(
                departmentAdministrator,
                otherDepartmentAdministrator,
                departmentAuthor,
                otherDepartmentAuthor,
                acceptedDepartmentMember,
                otherAcceptedDepartmentMember,
                pendingDepartmentMember,
                otherPendingDepartmentMember,
                rejectedDepartmentMember,
                otherRejectedDepartmentMember,
                postAdministrator,
                otherPostAdministrator,
                unprivileged);
        }

        List<UserRole> userRoles = new ArrayList<>();
        for (Resource resource : resources) {
            switch (resource.getScope()) {
                case DEPARTMENT:
                    userRoles.add(setUpUserRole(baseline, resource, departmentAdministrator, ADMINISTRATOR, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, departmentAuthor, AUTHOR, ACCEPTED));
                    userRoles.add(setUpUserRole(baseline, resource, acceptedDepartmentMember, MEMBER, ACCEPTED));

                    if (scope == POST) {
                        userRoles.add(setUpUserRole(baseline, resource, pendingDepartmentMember, MEMBER, PENDING));
                        userRoles.add(setUpUserRole(baseline, resource, rejectedDepartmentMember, MEMBER, REJECTED));
                    }
                    if (resource.getState() == REJECTED) {
                        userRoles.add(
                            setUpUserRole(baseline, resource, otherDepartmentAdministrator, ADMINISTRATOR, REJECTED));
                        userRoles.add(
                            setUpUserRole(baseline, resource, otherDepartmentAuthor, AUTHOR, ACCEPTED));
                        userRoles.add(
                            setUpUserRole(baseline, resource, otherAcceptedDepartmentMember, MEMBER, ACCEPTED));

                        if (scope == POST) {
                            userRoles.add(
                                setUpUserRole(baseline, resource, otherPendingDepartmentMember, MEMBER, PENDING));
                            userRoles.add(
                                setUpUserRole(baseline, resource, otherRejectedDepartmentMember, MEMBER, REJECTED));
                        }
                    }
                    continue;
                case POST:
                    if (resource.getParent().getParent().getState() == ACCEPTED) {
                        userRoles.add(setUpUserRole(baseline, resource, postAdministrator, ADMINISTRATOR, ACCEPTED));
                    } else {
                        userRoles.add(
                            setUpUserRole(baseline, resource, otherPostAdministrator, ADMINISTRATOR, ACCEPTED));
                    }
            }
        }

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
