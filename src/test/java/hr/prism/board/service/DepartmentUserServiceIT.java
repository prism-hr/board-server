package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.value.DemographicDataStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.time.LocalDate;

import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/departmentUser_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class DepartmentUserServiceIT {

    @Inject
    private UserService userService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DepartmentUserService departmentUserService;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    @Test
    public void makeDemographicDataStatus_successWhenComplete() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(1L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, false, false,
                UNDERGRADUATE_STUDENT, "memberProgram", 2018,
                LocalDate.of(2020, 6, 1));

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenIncomplete() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(2L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, true, true,
                null, null, null, null);

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserComplete() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(3L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, false, true,
                null, null, null, null);

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenMemberComplete() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(4L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, true, false,
                UNDERGRADUATE_STUDENT, "memberProgram", 2018,
                LocalDate.of(2020, 6, 1));

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserCompleteAndDepartmentWithoutCategories() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(1L);
            Department department = (Department) resourceService.getById(2L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);


            verifyDemographicData(dataStatus, false, false,
                null, null, null, null);

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserIncompleteAndDepartmentWithoutCategories() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(2L);
            Department department = (Department) resourceService.getById(2L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, true, false,
                null, null, null, null);

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserCompleteAndDepartmentAdministrator() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(5L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);


            verifyDemographicData(dataStatus, false, false,
                null, null, null, null);

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserIncompleteAndDepartmentAdministrator() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(6L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, true, false,
                null, null, null, null);

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserCompleteAndAcademicYearStartAfterMemberDate() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(7L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, false, true,
                UNDERGRADUATE_STUDENT, "memberProgram", 2018,
                LocalDate.of(2020, 6, 1));

            return null;
        });
    }

    @Test
    public void makeDemographicDataStatus_successWhenUserIncompleteAndAcademicYearStartAfterMemberDate() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(8L);
            Department department = (Department) resourceService.getById(1L);

            DemographicDataStatus dataStatus = departmentUserService.makeDemographicDataStatus(user, department);

            verifyDemographicData(dataStatus, true, true,
                UNDERGRADUATE_STUDENT, "memberProgram", 2018,
                LocalDate.of(2020, 6, 1));

            return null;
        });
    }

    private void verifyDemographicData(DemographicDataStatus dataStatus, Boolean expectedRequireUserData,
                                       Boolean expectedRequireMemberData, MemberCategory expectedMemberCategory,
                                       String expectedMemberProgram, Integer expectedMemberYear,
                                       LocalDate expectedExpiryDate) {
        assertEquals(expectedRequireUserData, dataStatus.isRequireUserData());
        assertEquals(expectedRequireMemberData, dataStatus.isRequireMemberData());
        assertEquals(expectedMemberCategory, dataStatus.getMemberCategory());
        assertEquals(expectedMemberProgram, dataStatus.getMemberProgram());
        assertEquals(expectedMemberYear, dataStatus.getMemberYear());
        assertEquals(expectedExpiryDate, dataStatus.getExpiryDate());
    }

}
