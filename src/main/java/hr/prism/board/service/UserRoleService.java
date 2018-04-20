package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.UserRoleMapper;
import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.representation.DemographicDataStatusRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.value.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Role.STAFF_ROLES;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.utils.BoardUtils.getAcademicYearStart;

@Service
@Transactional
public class UserRoleService {

    @Inject
    private UserRoleRepository userRoleRepository;

    @Inject
    private UserRoleCacheService userRoleCacheService;

//    @Inject
//    private ActivityService activityService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @Inject
    private UserCacheService userCacheService;

//    @Inject
//    private ActionService actionService;

    @Inject
    private UserRoleMapper userRoleMapper;

    @Inject
    private EntityManager entityManager;

    public UserRole fineOne(Long userRoleId) {
        return userRoleRepository.findOne(userRoleId);
    }

    public void createOrUpdateUserRole(Resource resource, User user, Role role) {
        createOrUpdateUserRole(user, resource, user, new UserRoleDTO().setRole(role));
    }

    public UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role) {
        return userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
    }

    public List<UserRole> findByResourceAndUser(Resource resource, User user) {
        return userRoleRepository.findByResourceAndEnclosingUser(resource, user);
    }

    public boolean hasAdministratorRole(User user) {
        return userRoleRepository.findIdsByUserAndRole(user, Role.ADMINISTRATOR, State.ACTIVE_USER_ROLE_STATES, LocalDate.now()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Statistics getMemberStatistics(Long departmentId) {
        return (Statistics) entityManager.createNamedQuery("memberStatistics")
            .setParameter("departmentId", departmentId)
            .getSingleResult();
    }

    public DemographicDataStatusRepresentation makeDemographicDataStatus(User user, Department department,
                                                                         boolean canPursue) {
        DemographicDataStatusRepresentation responseReadiness = new DemographicDataStatusRepresentation();
        if (Stream.of(user.getGender(), user.getAgeRange(), user.getLocationNationality()).anyMatch(Objects::isNull)) {
            // User data incomplete
            responseReadiness.setRequireUserDemographicData(true);
        }

        if (department.getMemberCategories().isEmpty()) {
            return responseReadiness;
        }

        UserRole userRole = findByResourceAndUserAndRole(department, user, MEMBER);
        if (userRole == null) {
            // Don't bug administrator for user role data
            responseReadiness.setRequireUserRoleDemographicData(!canPursue);
        } else {
            MemberCategory memberCategory = userRole.getMemberCategory();
            String memberProgram = userRole.getMemberProgram();
            Integer memberYear = userRole.getMemberYear();
            if (Stream.of(memberCategory, memberProgram, memberYear).anyMatch(Objects::isNull)) {
                // User role data incomplete
                responseReadiness.setRequireUserRoleDemographicData(true)
                    .setUserRole(
                        new UserRoleRepresentation()
                            .setMemberCategory(memberCategory)
                            .setMemberProgram(memberProgram)
                            .setMemberYear(memberYear));
            } else {
                LocalDate academicYearStart = getAcademicYearStart();
                if (academicYearStart.isAfter(userRole.getMemberDate())) {
                    // User role data out of date
                    responseReadiness.setRequireUserRoleDemographicData(true)
                        .setUserRole(
                            new UserRoleRepresentation()
                                .setMemberCategory(memberCategory)
                                .setMemberProgram(memberProgram)
                                .setMemberYear(memberYear));
                }
            }
        }

        return responseReadiness;
    }

    private UserRole createOrUpdateUserRole(User currentUser, Resource resource, User user, UserRoleDTO userRoleDTO) {

        Role role = userRoleDTO.getRole();
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
        if (userRole == null) {
            return userRoleCacheService.createUserRole(
                currentUser, resource, user, userRoleDTO, STAFF_ROLES.contains(role));
        } else {
            userRoleCacheService.updateMembershipData(userRole, userRoleDTO);
            userRole.setState(ACCEPTED);
            userRole.setExpiryDate(userRoleDTO.getExpiryDate());
            return userRole;
        }
    }

}
