package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.State;
import hr.prism.board.representation.ActionRepresentation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static hr.prism.board.utils.ResourceUtils.getQuarter;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@Component
public class ServiceVerificationHelper {

    @SuppressWarnings("SameParameterValue")
    void verifyDepartment(Department department, University expectedUniversity, String expectedName,
                          String expectedSummary, State expectedState, State expectedPreviousState,
                          String expectedHandle, MemberCategory[] expectedMemberCategories,
                          Action[] expectedActions, String expectedIndexData, LocalDateTime baseline) {
        verifyIdentity(department, expectedUniversity, expectedName);
        assertEquals(expectedSummary, department.getSummary());

        assertEquals(expectedState, department.getState());
        assertEquals(expectedPreviousState, department.getPreviousState());
        assertEquals(expectedHandle, department.getHandle());

        assertNull(department.getDocumentLogo());
        assertNull(department.getLocation());

        verifyCategories(department.getMemberCategories(),
            Stream.of(expectedMemberCategories).map(MemberCategory::name).toArray(String[]::new));
        verifyActions(department, expectedActions);

        verifyIndexDataAndQuarter(department, expectedIndexData);
        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThan(baseline);
        verifyTimestamps(department, baseline);
    }

    @SuppressWarnings("SameParameterValue")
    void verifyBoard(Board board, Department expectedDepartment, String expectedName, State expectedState,
                     State expectedPreviousState, String expectedHandle, String[] expectedPostCategories,
                     Action[] expectedActions, String expectedIndexData, LocalDateTime baseline) {
        verifyIdentity(board, expectedDepartment, expectedName);

        assertEquals(expectedState, board.getState());
        assertEquals(expectedPreviousState, board.getPreviousState());
        assertEquals(expectedHandle, board.getHandle());

        verifyCategories(board.getPostCategories(), expectedPostCategories);
        verifyActions(board, expectedActions);

        verifyIndexDataAndQuarter(board, expectedIndexData);
        verifyTimestamps(board, baseline);
    }

    private void verifyIdentity(Resource resource, Resource expectedParentResource, String expectedName) {
        assertNotNull(resource.getId());
        assertEquals(expectedParentResource, resource.getParent());
        assertEquals(expectedName, resource.getName());
    }

    private void verifyCategories(List<ResourceCategory> categories, String[] expectedCategories) {
        assertThat(
            categories
                .stream()
                .map(ResourceCategory::getName)
                .collect(toList()))
            .containsExactly(expectedCategories);
    }

    private void verifyActions(Resource resource, Action[] expectedActions) {
        assertThat(
            resource.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(expectedActions);
    }

    private void verifyIndexDataAndQuarter(Resource resource, String expectedIndexData) {
        assertEquals(expectedIndexData, resource.getIndexData());
        assertEquals(getQuarter(resource.getCreatedTimestamp()), resource.getQuarter());
    }

    private void verifyTimestamps(Resource resource, LocalDateTime baseline) {
        assertThat(resource.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(resource.getUpdatedTimestamp()).isGreaterThan(baseline);
    }

}
