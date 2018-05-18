package hr.prism.board.data;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.enums.State;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static hr.prism.board.enums.State.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Transactional
public class FixtureBuilder {

    private final ResourceService resourceService;

    public FixtureBuilder(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public String makeResourceInserts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Resource> resources = new ArrayList<>();

        University university = new University();
        university.setParent(university);
        university.setName("university");
        university.setHandle("university");
        university.setState(ACCEPTED);
        university.setPreviousState(ACCEPTED);
        university.setCreatedTimestamp(baseline);

        resources.add(university);
        resources.addAll(setUpDepartment(baseline, university, ACCEPTED));
        resources.addAll(setUpDepartment(baseline, university, REJECTED));

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            resource.setId((long) (i + 1));
            resourceService.setIndexDataAndQuarter(resource);

            rows.add(
                "(" +
                    resource.getId() + ", " +
                    resource.getParent().getId() + ", " +
                    "," + resource.getHandle() + "', " +
                    "," + resource.getState() + "', " +
                    "'" + resource.getPreviousState() + "', " +
                    "'" + resource.getIndexData() + "', " +
                    "'" + resource.getQuarter() + "', " +
                    "'" + Timestamp.valueOf(resource.getCreatedTimestamp()) + "'" +
                    ")");
        }

        List<String> inserts = ImmutableList.of(
            "INSERT INTO resource (id, parent_id, name, handle, state, previous_state, index_data, quarter, " +
                "created_timestamp\n" +
                "VALUES \n\t" + Joiner.on(",\n\t").join(rows) + ";",
            makeResourceRelationInsert(baseline, resources),
            makeUserInserts(baseline),
            makeUserRoleInserts(baseline));

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

        return "INSERT INTO resource_relation (id, resource1_id, resource2_id, created_timestamp\n" +
            "VALUES \n\t" + Joiner.on(",\n\t").join(rows) + ";";
    }

    private String makeUserInserts(LocalDateTime baseline) {
        return EMPTY;
    }

    private String makeUserRoleInserts(LocalDateTime baseline) {
        return EMPTY;
    }

    private List<Resource> setUpDepartment(LocalDateTime baseline, University university, State state) {
        Department department = new Department();
        department.setParent(university);

        List<Resource> resources = new ArrayList<>();
        resources.add(department);

        department.setName("department-" + state.name().toLowerCase());
        department.setHandle(university.getHandle() + "/" + department.getName());

        department.setState(state);
        department.setPreviousState(state);
        department.setCreatedTimestamp(baseline);

        for (State boardState : new State[]{ACCEPTED, REJECTED}) {
            resources.addAll(setUpBoard(baseline, department, boardState));
        }

        return resources;
    }

    private List<Resource> setUpBoard(LocalDateTime baseline, Department department, State state) {
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

        for (State postState :
            new State[]{DRAFT, PENDING, ACCEPTED, EXPIRED, SUSPENDED, REJECTED, WITHDRAWN, ARCHIVED}) {
            resources.add(setUpPost(baseline, board, postState));
        }

        return resources;
    }

    private Resource setUpPost(LocalDateTime baseline, Board board, State state) {
        Post post = new Post();
        post.setParent(board);

        String postName = "post-" + state.name().toLowerCase();
        post.setName(board.getName() + "-" + postName);
        post.setHandle(board.getHandle() + "/" + postName);

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

}
