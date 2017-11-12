package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.dto.*;
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
import org.apache.commons.collections.CollectionUtils;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@SuppressWarnings({"SqlResolve", "SpringAutowiredFieldsWarningInspection"})
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

    private static final List<String> MEMBER_CATEGORY_STRINGS = Stream.of(MemberCategory.values()).map(MemberCategory::name).collect(Collectors.toList());

    private static final String CAREER_NAME = "Career Opportunities";

    private static final String RESEARCH_NAME = "Research Opportunities";

    private static final String CAREER_SUMMARY = "Forum for partner organizations and staff to share career opportunities.";

    private static final String RESEARCH_SUMMARY = "Forum for partner organizations and staff to share research opportunities.";

    private static final List<String> CAREER_CATEGORIES = ImmutableList.of("Employment", "Internship", "Volunteering");

    private static final List<String> RESEARCH_CATEGORIES = ImmutableList.of("MRes", "PhD", "Postdoc");

    private static final List<hr.prism.board.enums.ResourceTask> MEMBER_TASKS = ImmutableList.of(
        hr.prism.board.enums.ResourceTask.CREATE_MEMBER, hr.prism.board.enums.ResourceTask.UPDATE_MEMBER);

    private static final List<hr.prism.board.enums.ResourceTask> DEPARTMENT_TASKS = ImmutableList.of(
        hr.prism.board.enums.ResourceTask.CREATE_MEMBER, hr.prism.board.enums.ResourceTask.CREATE_INTERNAL_POST, hr.prism.board.enums.ResourceTask.DEPLOY_BADGE);

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

    @Inject
    private BoardService boardService;

    @Inject
    private ResourceTaskService resourceTaskService;

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

    public List<Long> findAllIds(LocalDateTime baseline1, LocalDateTime baseline2) {
        return departmentRepository.findAllIds(baseline1, baseline2);
    }

    public Department getDepartment(Long id) {
        User user = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(user, Scope.DEPARTMENT, id);
        return verifyCanViewAndRemoveSuppressedTasks(user, department);
    }

    public Department getDepartment(String handle) {
        User user = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(user, Scope.DEPARTMENT, handle);
        return verifyCanViewAndRemoveSuppressedTasks(user, department);
    }

    public List<Department> getDepartments(Boolean includePublicDepartments, String searchTerm) {
        User user = userService.getCurrentUser();
        List<hr.prism.board.domain.ResourceTask> suppressedTasks = resourceTaskService.findBySuppressions(user);

        List<Resource> resources =
            resourceService.getResources(user,
                new ResourceFilter()
                    .setScope(Scope.DEPARTMENT)
                    .setSearchTerm(searchTerm)
                    .setIncludePublicResources(includePublicDepartments)
                    .setOrderStatement("resource.name"));

        List<Department> departments = new ArrayList<>();
        resources.forEach(resource -> {
            Department department = (Department) resource;
            department.getTasks().removeAll(suppressedTasks);
            departments.add(department);
        });

        return departments;
    }

    public Department createDepartment(Long universityId, DepartmentDTO departmentDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Resource university = universityService.getUniversity(universityId);
        resourceService.validateUniqueName(Scope.DEPARTMENT, null, university, departmentDTO.getName(), ExceptionCode.DUPLICATE_DEPARTMENT);
        String name = StringUtils.normalizeSpace(departmentDTO.getName());

        Department department = new Department();
        resourceService.updateState(department, State.ACCEPTED);
        department.setName(name);
        department.setSummary(departmentDTO.getSummary());

        DocumentDTO documentLogoDTO = departmentDTO.getDocumentLogo();
        if (documentLogoDTO == null) {
            department.setDocumentLogo(university.getDocumentLogo());
        } else {
            department.setDocumentLogo(documentService.getOrCreateDocument(documentLogoDTO));
        }

        String handle = resourceService.createHandle(university, name, departmentRepository::findHandleByLikeSuggestedHandle);
        resourceService.updateHandle(department, handle);
        department = departmentRepository.save(department);

        List<String> memberCategoryStrings;
        List<MemberCategory> memberCategories = departmentDTO.getMemberCategories();
        if (CollectionUtils.isEmpty(memberCategories)) {
            memberCategoryStrings = MEMBER_CATEGORY_STRINGS;
        } else {
            memberCategoryStrings = MemberCategory.toStrings(departmentDTO.getMemberCategories());
        }

        resourceService.updateCategories(department, CategoryType.MEMBER, memberCategoryStrings);
        resourceService.createResourceRelation(university, department);
        resourceService.setIndexDataAndQuarter(department);
        resourceService.createResourceOperation(department, Action.EXTEND, currentUser);
        userRoleService.createOrUpdateUserRole(department, currentUser, Role.ADMINISTRATOR);

        // Create the initial boards
        Long departmentId = department.getId();
        boardService.createBoard(departmentId,
            new BoardDTO().setType(BoardType.CAREER).setName(CAREER_NAME).setSummary(CAREER_SUMMARY).setPostCategories(CAREER_CATEGORIES));
        boardService.createBoard(departmentId,
            new BoardDTO().setType(BoardType.RESEARCH).setName(RESEARCH_NAME).setSummary(RESEARCH_SUMMARY).setPostCategories(RESEARCH_CATEGORIES));

        // Create the initial tasks
        department.setLastTaskCreationTimestamp(LocalDateTime.now());
        resourceTaskService.createForNewResource(departmentId, DEPARTMENT_TASKS);
        return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
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

    public Department putTask(Long departmentId, Long taskId) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(user, department, Action.EDIT, () -> {
            resourceTaskService.createSuppression(user, taskId);
            entityManager.flush();
            return resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        });
    }

    public Department postMembers(Long departmentId, List<UserRoleDTO> userRoleDTOs) {
        if (userRoleDTOs.stream().map(UserRoleDTO::getRole).anyMatch(role -> role != Role.MEMBER)) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_USER, "Only members can be bulk created");
        }

        User currentUser = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            department.increaseMemberTobeUploadedCount((long) userRoleDTOs.size());
            userRoleEventService.publishEvent(this, currentUser.getId(), departmentId, userRoleDTOs);
            resourceTaskService.deleteTasks(department, MEMBER_TASKS);
            department.setLastMemberTimestamp(LocalDateTime.now());
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
                activityEventService.publishEvent(this, departmentId, userRole);
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

    public void updateTasks(Long departmentId, LocalDateTime baseline) {
        List<hr.prism.board.enums.ResourceTask> tasks = new ArrayList<>();
        Department department = (Department) resourceService.findOne(departmentId);

        LocalDateTime lastMemberTimestamp = department.getLastMemberTimestamp();
        if (lastMemberTimestamp == null || lastMemberTimestamp.isBefore(baseline)) {
            tasks.add(hr.prism.board.enums.ResourceTask.UPDATE_MEMBER);
        }

        LocalDateTime lastInternalPostTimestamp = department.getLastInternalPostTimestamp();
        if (lastInternalPostTimestamp == null || lastInternalPostTimestamp.isBefore(baseline)) {
            tasks.add(hr.prism.board.enums.ResourceTask.UPDATE_INTERNAL_POST);
        }

        department.setLastTaskCreationTimestamp(baseline);
        resourceTaskService.createForExistingResource(departmentId, tasks);
    }

    void validateMembership(User user, Department department, Class<? extends BoardException> exceptionClass, ExceptionCode exceptionCode) {
        PostResponseReadinessRepresentation responseReadiness = makePostResponseReadiness(user, department, true);
        if (!responseReadiness.isReady()) {
            if (responseReadiness.isRequireUserDemographicData()) {
                BoardExceptionFactory.throwFor(exceptionClass, exceptionCode, "User demographic data not valid");
            }

            BoardExceptionFactory.throwFor(exceptionClass, exceptionCode, "User role demographic data not valid");
        }
    }

    PostResponseReadinessRepresentation makePostResponseReadiness(User user, Department department, boolean canPursue) {
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

    private Department verifyCanViewAndRemoveSuppressedTasks(User user, Department department) {
        return (Department) actionService.executeAction(user, department, Action.VIEW, () -> {
            List<ResourceTask> suppressedTasks = resourceTaskService.findByResourceAndSuppressions(department, user);
            department.getTasks().removeAll(suppressedTasks);
            return department;
        });
    }

}
