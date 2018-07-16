package hr.prism.board.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;

import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.PENDING;
import static hr.prism.board.enums.State.REJECTED;
import static hr.prism.board.utils.BoardUtils.makeAcademicYearStart;
import static java.time.LocalDate.now;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class UserRoleTest {

    @Test
    public void isActiveResourceRole_successWhenAcceptedAndExpiry() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertTrue(
            new UserRole()
                .setResource(resource)
                .setRole(ADMINISTRATOR)
                .setState(ACCEPTED)
                .setExpiryDate(now().plusYears(1))
                .isActiveResourceRole(resource, ADMINISTRATOR));
    }

    @Test
    public void isActiveResourceRole_successWhenAcceptedAndNoExpiry() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertTrue(
            new UserRole()
                .setResource(resource)
                .setRole(ADMINISTRATOR)
                .setState(ACCEPTED)
                .isActiveResourceRole(resource, ADMINISTRATOR));
    }

    @Test
    public void isActiveResourceRole_successWhenPendingAndExpiry() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertTrue(
            new UserRole()
                .setResource(resource)
                .setRole(MEMBER)
                .setState(PENDING)
                .setExpiryDate(now().plusYears(1))
                .isActiveResourceRole(resource, MEMBER));
    }

    @Test
    public void isActiveResourceRole_successWhenPendingAndNoExpiry() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertTrue(
            new UserRole()
                .setResource(resource)
                .setRole(MEMBER)
                .setState(PENDING)
                .isActiveResourceRole(resource, MEMBER));
    }

    @Test
    public void isActiveResourceRole_successWhenWrongResource() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertFalse(
            new UserRole()
                .setResource(resource)
                .setRole(ADMINISTRATOR)
                .setState(ACCEPTED)
                .isActiveResourceRole(new Resource(), ADMINISTRATOR));
    }

    @Test
    public void isActiveResourceRole_successWhenWrongRole() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertFalse(
            new UserRole()
                .setResource(resource)
                .setRole(ADMINISTRATOR)
                .setState(ACCEPTED)
                .isActiveResourceRole(resource, MEMBER));
    }

    @Test
    public void isActiveResourceRole_successWhenExpired() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertFalse(
            new UserRole()
                .setResource(resource)
                .setRole(MEMBER)
                .setState(ACCEPTED)
                .setExpiryDate(now().minusYears(1))
                .isActiveResourceRole(resource, MEMBER));
    }

    @Test
    public void isActiveResourceRole_successWhenRejected() {
        Resource resource = new Resource();
        resource.setId(1L);

        assertFalse(
            new UserRole()
                .setResource(resource)
                .setRole(MEMBER)
                .setState(REJECTED)
                .setExpiryDate(now().plusYears(1))
                .isActiveResourceRole(resource, MEMBER));
    }

    @Test
    public void isResponseDataIncomplete_successWhenEmpty() {
        assertTrue(new UserRole().isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoCategory() {
        assertTrue(
            new UserRole()
                .setMemberProgram("program")
                .setMemberYear(2018)
                .setMemberDate(now())
                .setExpiryDate(now().plusYears(1))
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoProgram() {
        assertTrue(
            new UserRole()
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberYear(2018)
                .setMemberDate(now())
                .setExpiryDate(now().plusYears(1))
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoMemberYear() {
        assertTrue(
            new UserRole()
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberDate(now())
                .setExpiryDate(now().plusYears(1))
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNoExpiryDate() {
        assertTrue(
            new UserRole()
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberYear(2018)
                .setMemberDate(now())
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenNewAcademicYear() {
        LocalDate memberDate = makeAcademicYearStart().minusDays(1);
        assertTrue(
            new UserRole()
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberYear(2018)
                .setMemberDate(memberDate)
                .setExpiryDate(now().plusYears(1))
                .isResponseDataIncomplete());
    }

    @Test
    public void isResponseDataIncomplete_successWhenComplete() {
        assertFalse(
            new UserRole()
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("program")
                .setMemberYear(2018)
                .setMemberDate(now())
                .setExpiryDate(now().plusYears(1))
                .isResponseDataIncomplete());
    }

}
