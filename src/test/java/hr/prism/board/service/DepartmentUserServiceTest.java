package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.event.EventProducer;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.value.DemographicDataStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;

import static hr.prism.board.enums.AgeRange.ZERO_EIGHTEEN;
import static hr.prism.board.enums.Gender.FEMALE;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_REFERRAL;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentUserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private ActionService actionService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private ActivityService activityService;

    @Mock
    private ResourceTaskService resourceTaskService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private DepartmentUserService departmentUserService;

    @Before
    public void setUp() {
        departmentUserService = new DepartmentUserService(userService, resourceService, actionService, userRoleService,
            resourceTaskService, activityService, eventProducer);
    }

    @Test
    public void validateDemographicData_success() {
        departmentUserService.checkValidDemographicData(new DemographicDataStatus());
    }

    @Test
    public void makeDemographicData_successWhenAdministrator() {
        Department department1 = makeDepartment(1L);
        Department department2 = makeDepartment(2L);

        UserRole userRole1 = makeUserRoleSpy(1L, department1, ADMINISTRATOR);
        UserRole userRole2 = makeUserRoleSpy(2L, department2, MEMBER);

        User user = new User();
        user.setUserRoles(newLinkedHashSet(userRole1, userRole2));

        DemographicDataStatus demographicDataStatus =
            departmentUserService.makeDemographicDataStatus(user, department1);

        assertEquals(ADMINISTRATOR, demographicDataStatus.getRole());
        assertFalse(demographicDataStatus.isRequireUserData());
        assertFalse(demographicDataStatus.isRequireMemberData());

        verify(userRole1, times(1)).isActiveResourceRole(department1, MEMBER);
        verify(userRole2, times(1)).isActiveResourceRole(department1, MEMBER);
    }

    @Test
    public void makeDemographicData_successWhenMemberAndDepartmentCategories() {
        Department department1 = makeDepartment(1L);
        department1.setCategories(newLinkedHashSet(new ResourceCategory().setType(CategoryType.MEMBER)));

        Department department2 = makeDepartment(2L);

        UserRole userRole1 = makeUserRoleSpy(2L, department2, ADMINISTRATOR);
        UserRole userRole2 = makeUserRoleSpy(1L, department1, MEMBER);

        User user = makeUserSpy(1L);
        user.setUserRoles(newLinkedHashSet(userRole1, userRole2));

        DemographicDataStatus demographicDataStatus =
            departmentUserService.makeDemographicDataStatus(user, department1);

        assertEquals(MEMBER, demographicDataStatus.getRole());
        assertTrue(demographicDataStatus.isRequireUserData());
        assertTrue(demographicDataStatus.isRequireMemberData());

        verify(user, times(1)).isResponseDataIncomplete();

        verify(userRole1, times(1)).isActiveResourceRole(department1, MEMBER);

        verify(userRole2, times(1)).isActiveResourceRole(department1, MEMBER);
        verify(userRole2, times(1)).isResponseDataIncomplete();
    }

    @Test
    public void makeDemographicData_successWhenMemberAndDepartmentCategoriesAndUserAndMemberData() {
        Department department1 = makeDepartment(1L);
        department1.setCategories(newLinkedHashSet(new ResourceCategory().setType(CategoryType.MEMBER)));

        Department department2 = makeDepartment(2L);

        UserRole userRole1 = makeUserRoleSpy(2L, department2, ADMINISTRATOR);

        LocalDate expiryDate = now().plusYears(1);
        UserRole userRole2 =
            makeUserRoleSpy(1L, department1, MEMBER)
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberYear(2018)
                .setMemberDate(now().plusMonths(1))
                .setExpiryDate(expiryDate);

        User user =
            makeUserSpy(1L)
                .setGender(FEMALE)
                .setAgeRange(ZERO_EIGHTEEN)
                .setLocationNationality(new Location())
                .setUserRoles(newLinkedHashSet(userRole1, userRole2));

        DemographicDataStatus demographicDataStatus =
            departmentUserService.makeDemographicDataStatus(user, department1);

        assertEquals(MEMBER, demographicDataStatus.getRole());
        assertEquals(UNDERGRADUATE_STUDENT, demographicDataStatus.getMemberCategory());
        assertEquals("program", demographicDataStatus.getMemberProgram());
        assertEquals(2018, demographicDataStatus.getMemberYear().intValue());
        assertEquals(expiryDate, demographicDataStatus.getExpiryDate());
        assertFalse(demographicDataStatus.isRequireUserData());
        assertFalse(demographicDataStatus.isRequireMemberData());

        verify(user, times(1)).isResponseDataIncomplete();

        verify(userRole1, times(1)).isActiveResourceRole(department1, MEMBER);

        verify(userRole2, times(1)).isActiveResourceRole(department1, MEMBER);
        verify(userRole2, times(1)).isResponseDataIncomplete();
    }

    @Test
    public void makeDemographicData_successWhenMemberAndNoDepartmentCategories() {
        Department department1 = makeDepartment(1L);
        Department department2 = makeDepartment(2L);

        UserRole userRole1 = makeUserRoleSpy(2L, department2, ADMINISTRATOR);
        UserRole userRole2 = makeUserRoleSpy(1L, department1, MEMBER);

        User user = makeUserSpy(1L);
        user.setUserRoles(newLinkedHashSet(userRole1, userRole2));

        DemographicDataStatus demographicDataStatus =
            departmentUserService.makeDemographicDataStatus(user, department1);

        assertEquals(MEMBER, demographicDataStatus.getRole());
        assertTrue(demographicDataStatus.isRequireUserData());
        assertFalse(demographicDataStatus.isRequireMemberData());

        verify(user, times(1)).isResponseDataIncomplete();

        verify(userRole1, times(1)).isActiveResourceRole(department1, MEMBER);
        verify(userRole2, times(1)).isActiveResourceRole(department1, MEMBER);
    }

    @Test
    public void validateDemographicData_failureWhenRequireUserData() {
        assertThatThrownBy(() ->
            departmentUserService.checkValidDemographicData(
                new DemographicDataStatus()
                    .setRequireUserData(true)))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_REFERRAL)
            .hasMessageContaining("User data not valid / complete");
    }

    @Test
    public void validateDemographicData_failureWhenRequireMemberData() {
        assertThatThrownBy(() ->
            departmentUserService.checkValidDemographicData(
                new DemographicDataStatus()
                    .setRequireMemberData(true)))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_REFERRAL)
            .hasMessageContaining("Member data not valid / complete");
    }

    @Test
    public void validateDemographicData_failureWhenRequireUserAndMemberData() {
        assertThatThrownBy(() ->
            departmentUserService.checkValidDemographicData(
                new DemographicDataStatus()
                    .setRequireUserData(true)
                    .setRequireMemberData(true)))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_REFERRAL)
            .hasMessageContaining("User data not valid / complete");
    }

    private static Department makeDepartment(Long id) {
        Department department = new Department();
        department.setId(id);
        return department;
    }

    @SuppressWarnings("SameParameterValue")
    private static User makeUserSpy(Long id) {
        User user = new User();
        user.setId(id);
        return spy(user);
    }

    private static UserRole makeUserRoleSpy(Long id, Resource resource, Role role) {
        UserRole userRole =
            new UserRole()
                .setResource(resource)
                .setRole(role)
                .setState(ACCEPTED);

        userRole.setId(id);
        return spy(userRole);
    }

}
