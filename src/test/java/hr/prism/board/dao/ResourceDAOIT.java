package hr.prism.board.dao;

import hr.prism.board.DBTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.repository.DocumentRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.value.ResourceFilter;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_BOARD;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceDAO_setUp.sql")
@Sql(value = "classpath:data/resourceDAO_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceDAOIT {

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    private DocumentRepository documentRepository;

    @Test
    public void checkUniqueName_successWhenCreateDepartment() {
        University university = new University();
        university.setId(1L);
        resourceDAO.checkUniqueName(DEPARTMENT, null, university, "new department", DUPLICATE_DEPARTMENT);
    }

    @Test
    public void checkUniqueName_failureWhenCreateDepartmentDuplicate() {
        University university = new University();
        university.setId(1L);
        Assertions.assertThatThrownBy(() ->
            resourceDAO.checkUniqueName(DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasMessage("DUPLICATE_DEPARTMENT: DEPARTMENT with name department exists already")
            .hasFieldOrPropertyWithValue("id", 2L);
    }

    @Test
    public void checkUniqueName_successWhenCreateBoard() {
        University university = new University();
        university.setId(1L);
        resourceDAO.checkUniqueName(DEPARTMENT, null, university, "new board", DUPLICATE_DEPARTMENT);
    }

    @Test
    public void checkUniqueName_failureWhenCreateBoardDuplicate() {
        Department department = new Department();
        department.setId(2L);
        Assertions.assertThatThrownBy(() ->
            resourceDAO.checkUniqueName(BOARD, null, department, "board", DUPLICATE_BOARD))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasMessage("DUPLICATE_BOARD: BOARD with name board exists already")
            .hasFieldOrPropertyWithValue("id", 3L);
    }

    @Test
    public void getResource_successWhenDepartmentDraftAdministrator() {
        User user = userRepository.findOne(1L);
        List<Resource> resources = resourceDAO.getResources(user,
            new ResourceFilter()
                .setScope(DEPARTMENT)
                .setId(2L));

        assertThat(resources).hasSize(1);

        Department department = (Department) resources.get(0);
        University university = (University) resourceRepository.findOne(1L);
        Document documentLogo = documentRepository.findOne(1L);

        assertEquals(university, department.getParent());
        assertEquals(DEPARTMENT, department.getScope());
        assertEquals(DRAFT, department.getState());
        assertEquals(documentLogo, department.getDocumentLogo());

        assertThat(
            department.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactlyInAnyOrder(VIEW, EDIT, EXTEND, SUBSCRIBE);

        assertThat(
            department.getMemberCategories()
                .stream()
                .map(ResourceCategory::getName)
                .collect(toList()))
            .containsExactlyInAnyOrder("UNDERGRADUATE_STUDENT", "MASTER_STUDENT");
    }

}
