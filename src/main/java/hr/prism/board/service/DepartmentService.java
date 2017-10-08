package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardExceptionFactory;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.*;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.service.event.UserRoleEventService;
import hr.prism.board.value.ResourceFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@SuppressWarnings("SqlResolve")
public class DepartmentService {

    private static final String SIMILAR_DEPARTMENT =
        "SELECT resource.id, resource.name, document_logo.cloudinary_id, document_logo.cloudinary_url, document_logo.file_name, " +
            "IF(resource.name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH (resource.name) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "LEFT JOIN document AS document_logo " +
            "ON resource.document_logo_id = document_logo.id " +
            "WHERE resource.scope = :scope AND resource.state = :state " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.name " +
            "LIMIT 10";

    private static final String SIMILAR_DEPARTMENT_PROGRAM =
        "SELECT user_role.member_program, " +
            "IF(user_role.member_program LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH (user_role.member_program) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM user_role " +
            "WHERE user_role.resource_id = :departmentId " +
            "GROUP BY user_role.member_program " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, user_role.member_program " +
            "LIMIT 10";

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourcePatchService resourcePatchService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private ActionService actionService;

    @Inject
    private ActivityService activityService;

    @Inject
    private UserRoleEventService userRoleEventService;

    @Inject
    private UniversityService universityService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public Department getDepartment(Long id) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, id);
        return (Department) actionService.executeAction(currentUser, department, Action.VIEW, () -> department);
    }

    public Department getDepartment(String handle) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, handle);
        if (department != null) {
            return (Department) actionService.executeAction(currentUser, department, Action.VIEW, () -> department);
        }
        return null;
    }

    public List<Long> findAllIds() {
        return departmentRepository.findAllIds();
    }

    public List<Department> getDepartments(Boolean includePublicDepartments, String searchTerm) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilter()
                .setScope(Scope.DEPARTMENT)
                .setSearchTerm(searchTerm)
                .setIncludePublicResources(includePublicDepartments)
                .setOrderStatement("resource.name"))
            .stream().map(resource -> (Department) resource).collect(Collectors.toList());
    }

    public Department createDepartment(DepartmentDTO departmentDTO) {
        User currentUser = userService.getCurrentUserSecured();
        resourceService.validateUniqueName(Scope.DEPARTMENT, null, null, departmentDTO.getName(), ExceptionCode.DUPLICATE_DEPARTMENT);
        return getOrCreateDepartment(currentUser, departmentDTO);
    }

    public Department getOrCreateDepartment(User currentUser, DepartmentDTO departmentDTO) {
        Long id = departmentDTO.getId();
        String name = StringUtils.normalizeSpace(departmentDTO.getName());

        Department departmentById = null;
        Department departmentByName = null;
        for (Department department : departmentRepository.findByIdOrName(id, name)) {
            if (department.getId().equals(id)) {
                departmentById = department;
            }

            if (department.getName().equals(name)) {
                departmentByName = department;
                break;
            }
        }

        if (departmentById != null) {
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentById.getId());
        } else if (departmentByName != null) {
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentByName.getId());
        } else {
            Department department = new Department();
            resourceService.updateState(department, State.ACCEPTED);
            department.setName(name);
            department.setSummary(departmentDTO.getSummary());
            if (departmentDTO.getDocumentLogo() != null) {
                department.setDocumentLogo(documentService.getOrCreateDocument(departmentDTO.getDocumentLogo()));
            }

            Resource university = universityService.getOrCreateUniversity(UniversityService.UNIVERSITY_COLLEGE_LONDON, UniversityService.UCL);
            String handle = resourceService.createHandle(university, name, departmentRepository::findHandleByLikeSuggestedHandle);
            resourceService.updateHandle(department, handle);
            department = departmentRepository.save(department);

            resourceService.updateCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(departmentDTO.getMemberCategories()));
            resourceService.createResourceRelation(university, department);
            resourceService.setIndexDataAndQuarter(department);
            resourceService.createResourceOperation(department, Action.EXTEND, currentUser);
            userRoleService.createOrUpdateUserRole(department, currentUser, Role.ADMINISTRATOR);
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
        }
    }

    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            department.setChangeList(new ChangeListRepresentation());
            resourcePatchService.patchName(department, departmentDTO.getName(), ExceptionCode.DUPLICATE_DEPARTMENT);
            resourcePatchService.patchProperty(department, "summary", department::getSummary, department::setSummary, departmentDTO.getSummary());
            resourcePatchService.patchHandle(department, departmentDTO.getHandle(), ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
            resourcePatchService.patchDocument(department, "documentLogo", department::getDocumentLogo, department::setDocumentLogo, departmentDTO.getDocumentLogo());
            resourcePatchService.patchCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(departmentDTO.getMemberCategories()));
            departmentRepository.update(department);
            resourceService.setIndexDataAndQuarter(department);
            return department;
        });
    }

    public List<DepartmentRepresentation> findBySimilarName(String searchTerm) {
        List<Object[]> rows = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createNativeQuery(SIMILAR_DEPARTMENT)
                .setParameter("searchTermHard", searchTerm + "%")
                .setParameter("searchTermSoft", searchTerm)
                .setParameter("scope", Scope.DEPARTMENT.name())
                .setParameter("state", State.ACCEPTED.name())
                .getResultList());

        List<DepartmentRepresentation> departmentRepresentations = new ArrayList<>();
        for (Object[] row : rows) {
            DepartmentRepresentation departmentRepresentation =
                new DepartmentRepresentation().setId(Long.parseLong(row[0].toString())).setName(row[1].toString());
            Object cloudinaryId = row[2];
            if (cloudinaryId != null) {
                DocumentRepresentation documentLogoRepresentation =
                    new DocumentRepresentation().setCloudinaryId(cloudinaryId.toString()).setCloudinaryUrl(row[3].toString()).setFileName(row[4].toString());
                departmentRepresentation.setDocumentLogo(documentLogoRepresentation);
            }

            departmentRepresentations.add(departmentRepresentation);
        }

        return departmentRepresentations;
    }

    public List<String> findProgramsBySimilarName(Long departmentId, String searchTerm) {
        List<Object[]> rows = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createNativeQuery(SIMILAR_DEPARTMENT_PROGRAM)
                .setParameter("searchTermHard", searchTerm + "%")
                .setParameter("searchTermSoft", searchTerm)
                .setParameter("departmentId", departmentId)
                .getResultList());
        return rows.stream().map(row -> row[0].toString()).collect(Collectors.toList());
    }

    public Department postMembers(Long departmentId, List<UserRoleDTO> userRoleDTOs) {
        if (userRoleDTOs.stream().map(UserRoleDTO::getRole).anyMatch(role -> role != Role.MEMBER)) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_USER, "Only members can be bulk created");
        }

        User currentUser = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            department.addToMemberToBeUploadedCount((long) userRoleDTOs.size());
            userRoleEventService.publishEvent(this, currentUser.getId(), departmentId, userRoleDTOs);
            return department;
        });
    }

    public User postMembershipRequest(Long departmentId, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured(true);
        Department department = (Department) resourceService.findOne(departmentId);

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, Role.MEMBER);
        if (userRole != null) {
            if (userRole.getState() == State.REJECTED) {
                // User has been rejected already, don't let them be a nuisance by repeatedly retrying
                throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PERMISSION, "User has already been rejected as a member");
            }

            throw new BoardException(ExceptionCode.DUPLICATE_PERMISSION, "User has already requested membership");
        }


        UserDTO userDTO = userRoleDTO.getUser();
        if (userDTO != null) {
            // We validate the membership later - avoid NPE now
            userService.updateUserDemographicData(user, userDTO);
        }

        userRoleDTO.setRole(Role.MEMBER);
        userRole = userRoleCacheService.createUserRole(user, department, user, userRoleDTO, State.PENDING, false);
        validateMembership(user, department, BoardException.class, ExceptionCode.INVALID_MEMBERSHIP);

        hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
            .setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR).setActivity(hr.prism.board.enums.Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY);
        activityEventService.publishEvent(this, departmentId, userRole, Collections.singletonList(activity));

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR).setNotification(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION);
        notificationEventService.publishEvent(this, departmentId, Collections.singletonList(notification));
        return user;
    }

    public UserRole viewMembershipRequest(Long departmentId, Long userId) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        actionService.executeAction(user, department, Action.EDIT, () -> department);
        UserRole userRole = userRoleService.findByResourceAndUserIdAndRole(department, userId, Role.MEMBER);
        activityService.viewActivity(userRole.getActivity(), user);
        return userRole.setViewed(true);
    }

    public void putMembershipRequest(Long departmentId, Long userId, State state) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        actionService.executeAction(user, department, Action.EDIT, () -> {
            UserRole userRole = userRoleService.findByResourceAndUserIdAndRole(department, userId, Role.MEMBER);
            if (userRole.getState() == State.PENDING) {
                userRole.setState(state);
                activityEventService.publishEvent(this, departmentId, userRole.getId());
            }

            return department;
        });
    }

    public User putMembershipUpdate(Long departmentId, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured(true);
        Department department = (Department) resourceService.findOne(departmentId);

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, Role.MEMBER);
        if (userRole == null || userRole.getState() == State.REJECTED) {
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PERMISSION, "User is not a member");
        }

        UserDTO userDTO = userRoleDTO.getUser();
        if (userDTO != null) {
            // We validate the membership later - avoid NPE now
            userService.updateUserDemographicData(user, userDTO);
        }

        userRoleCacheService.updateUserRoleDemographicData(userRole, userRoleDTO);
        validateMembership(user, department, BoardException.class, ExceptionCode.INVALID_MEMBERSHIP);
        return user;
    }

    public void decrementMemberCountPending(Long departmentId) {
        ((Department) resourceService.findOne(departmentId)).decrementMemberToBeUploadedCount();
    }

    public void migrate(Long id) {
        Department department = (Department) resourceService.findOne(id);
        if (department.getHandle() == null) {
            String handle = resourceService.createHandle(department.getParent(), department.getName(), departmentRepository::findHandleByLikeSuggestedHandle);
            department.setHandle(handle);
        }

        resourceService.setIndexDataAndQuarter(department);
        userRoleCacheService.updateUserRolesSummary(department);
    }

    public void validateMembership(User user, Department department, Class<? extends BoardException> exceptionClass, ExceptionCode exceptionCode) {
        PostResponseReadinessRepresentation responseReadiness = makePostResponseReadiness(user, department, true);
        if (!responseReadiness.isReady()) {
            if (responseReadiness.isRequireUserDemographicData()) {
                BoardExceptionFactory.throwFor(exceptionClass, exceptionCode, "User demographic data not valid");
            }

            BoardExceptionFactory.throwFor(exceptionClass, exceptionCode, "User role demographic data not valid");
        }
    }

    public PostResponseReadinessRepresentation makePostResponseReadiness(User user, Department department, boolean canPursue) {
        PostResponseReadinessRepresentation responseReadiness = new PostResponseReadinessRepresentation();
        if (Stream.of(user.getGender(), user.getAgeRange(), user.getLocationNationality()).anyMatch(Objects::isNull)) {
            // User data incomplete
            responseReadiness.setRequireUserDemographicData(true);
        }

        if (!department.getMemberCategories().isEmpty()) {
            // Member category required - user role data expected
            UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, Role.MEMBER);
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
                        .setUserRole(new UserRoleRepresentation().setMemberCategory(memberCategory).setMemberProgram(memberProgram).setMemberYear(memberYear));
                } else {
                    LocalDate academicYearStart;
                    LocalDate baseline = LocalDate.now();
                    if (baseline.getMonthValue() > 9) {
                        // Academic year started this year
                        academicYearStart = LocalDate.of(baseline.getYear(), 10, 1);
                    } else {
                        // Academic year started last year
                        academicYearStart = LocalDate.of(baseline.getYear() - 1, 10, 1);
                    }

                    if (academicYearStart.isAfter(userRole.getMemberDate())) {
                        // User role data out of date
                        responseReadiness.setRequireUserRoleDemographicData(true)
                            .setUserRole(new UserRoleRepresentation().setMemberCategory(memberCategory).setMemberProgram(memberProgram).setMemberYear(memberYear));
                    }
                }
            }
        }

        return responseReadiness;
    }

}
