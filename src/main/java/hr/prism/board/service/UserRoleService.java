package hr.prism.board.service;

import hr.prism.board.dao.UserRoleDAO;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.MemberDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.RoleType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.value.Statistics;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;

import static hr.prism.board.enums.Role.*;
import static hr.prism.board.exception.ExceptionCode.IRREMOVABLE_USER;
import static hr.prism.board.exception.ExceptionCode.IRREMOVABLE_USER_ROLE;
import static hr.prism.board.utils.BoardUtils.emptyToNull;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

@Service
@Transactional
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    private final UserRoleDAO userRoleDAO;

    private final EntityManager entityManager;

    @Inject
    public UserRoleService(UserRoleRepository userRoleRepository, UserRoleDAO userRoleDAO,
                           EntityManager entityManager) {
        this.userRoleRepository = userRoleRepository;
        this.userRoleDAO = userRoleDAO;
        this.entityManager = entityManager;
    }

    public UserRole getById(Long id) {
        return userRoleRepository.findOne(id);
    }

    public UserRole getByUuid(String uuid) {
        return userRoleRepository.findByUuid(uuid);
    }

    public List<Role> getByResourceAndUser(Resource resource, User user) {
        return userRoleRepository.findByResourceAndUser(resource, user);
    }

    public UserRole getByResourceUserAndRole(Resource resource, User user, Role role) {
        return userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
    }

    public UserRole getByResourceAndUserIdAndRole(Resource resource, Long userId, Role role) {
        return userRoleRepository.findByResourceAndUserIdAndRole(resource, userId, role);
    }

    public List<UserRole> getUserRoles(Resource resource, List<Role> roles, State state, String searchTerm) {
        return userRoleDAO.getUserRoles(resource, roles, state, searchTerm);
    }

    public UserRole createUserRole(Resource resource, User user, Role role) {
        UserRole userRole =
            new UserRole()
                .setUuid(randomUUID().toString())
                .setResource(resource)
                .setUser(user)
                .setRole(role)
                .setCreated(true);

        userRole.setCreatorId(resource.getCreatorId());
        return userRoleRepository.save(userRole);
    }

    @SuppressWarnings("UnusedReturnValue")
    public UserRole getOrCreateUserRole(Resource resource, User userCreateUpdate, Role role) {
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, userCreateUpdate, role);
        if (userRole == null) {
            return createUserRole(resource, userCreateUpdate, role);
        }

        return userRole;
    }

    public UserRole createOrUpdateUserRole(Resource resource, User userCreateUpdate, MemberDTO memberDTO, State state) {
        UserRole userRole = getByResourceUserAndRole(resource, userCreateUpdate, Role.MEMBER);
        if (userRole == null) {
            return createUserRole(resource, userCreateUpdate, memberDTO, state);
        }

        userRole = updateMembership(userRole, memberDTO);
        userRole.setState(state);
        return userRole;
    }

    public UserRole updateMembership(UserRole userRole, MemberDTO memberDTO) {
        boolean updated = false;
        boolean clearStudyData = false;

        MemberCategory oldMemberCategory = userRole.getMemberCategory();
        MemberCategory newMemberCategory = memberDTO.getMemberCategory();
        if (newMemberCategory != null) {
            userRole.setMemberCategory(newMemberCategory);
            clearStudyData = newMemberCategory != oldMemberCategory;
            updated = true;
        }

        String memberProgram = memberDTO.getMemberProgram();
        if (memberProgram != null || clearStudyData) {
            userRole.setMemberProgram(memberProgram);
            updated = true;
        }

        Integer memberYear = memberDTO.getMemberYear();
        if (memberYear != null || clearStudyData) {
            userRole.setMemberYear(memberYear);
            updated = true;
        }

        if (updated) {
            userRole.setMemberDate(LocalDate.now());
        }

        return userRole;
    }

    public void deleteUserRoles(Resource resource, User user, RoleType roleType) {
        switch (roleType) {
            case STAFF:
                userRoleRepository.deleteByResourceAndUserAndRoles(resource, user, STAFF_ROLES);
                checkLastAdministratorNotRemoved(resource, IRREMOVABLE_USER);
                return;
            case MEMBER:
                userRoleRepository.deleteByResourceAndUserAndRoles(resource, user, MEMBER_ROLES);
                return;
            default:
                throw new IllegalStateException("Unexpected role type: " + roleType);
        }
    }

    public void deleteUserRole(Resource resource, User user, Role role) {
        userRoleRepository.deleteByResourceAndUserAndRoles(resource, user, singletonList(role));
        checkLastAdministratorNotRemoved(resource, IRREMOVABLE_USER_ROLE);
    }

    public void mergeUserRoles(User newUser, User oldUser) {
        Map<Pair<Resource, Role>, UserRole> newUserRoles = new HashMap<>();
        Map<Pair<Resource, Role>, UserRole> oldUserRoles = new HashMap<>();
        userRoleRepository.findByUsersOrderByUser(Arrays.asList(newUser, oldUser)).forEach(userRole -> {
            if (userRole.getUser().equals(newUser)) {
                newUserRoles.put(Pair.of(userRole.getResource(), userRole.getRole()), userRole);
            } else {
                oldUserRoles.put(Pair.of(userRole.getResource(), userRole.getRole()), userRole);
            }
        });

        List<UserRole> deletes = new ArrayList<>();
        for (Map.Entry<Pair<Resource, Role>, UserRole> oldUserRoleEntry : oldUserRoles.entrySet()) {
            if (newUserRoles.containsKey(oldUserRoleEntry.getKey())) {
                deletes.add(oldUserRoleEntry.getValue());
            }
        }

        userRoleDAO.deleteUserRoles(deletes);
        userRoleRepository.updateByUser(newUser, oldUser);
    }

    public Statistics getMemberStatistics(Long departmentId) {
        return userRoleDAO.getMemberStatistics(departmentId);
    }

    private UserRole createUserRole(Resource resource, User user, MemberDTO memberDTO, State state) {
        UserRole userRole =
            new UserRole()
                .setUuid(randomUUID().toString())
                .setResource(resource)
                .setUser(user)
                .setEmail(emptyToNull(memberDTO.getEmail()))
                .setRole(Role.MEMBER)
                .setMemberCategory(memberDTO.getMemberCategory())
                .setMemberProgram(memberDTO.getMemberProgram())
                .setMemberYear(memberDTO.getMemberYear())
                .setMemberDate(LocalDate.now())
                .setState(state)
                .setExpiryDate(memberDTO.getExpiryDate());

        userRole.setCreatorId(resource.getCreatorId());
        return userRoleRepository.save(userRole);
    }

    private void checkLastAdministratorNotRemoved(Resource resource, ExceptionCode exceptionCode) {
        entityManager.flush();
        List<UserRole> remainingAdministrators = userRoleRepository.findByResourceAndRole(resource, ADMINISTRATOR);
        if (remainingAdministrators.isEmpty()) {
            throw new BoardException(exceptionCode, "Cannot remove last remaining administrator");
        }
    }

}
