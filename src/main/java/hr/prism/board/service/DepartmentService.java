package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.stripe.model.*;
import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.representation.PostResponseReadinessRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.service.event.UserRoleEventService;
import hr.prism.board.value.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Activity.*;
import static hr.prism.board.enums.MemberCategory.MEMBER_CATEGORY_STRINGS;
import static hr.prism.board.enums.MemberCategory.toStrings;
import static hr.prism.board.enums.Notification.*;
import static hr.prism.board.enums.ResourceTask.DEPARTMENT_TASKS;
import static hr.prism.board.enums.ResourceTask.UPDATE_MEMBER;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.BoardExceptionFactory.throwFor;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.getAcademicYearStart;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class DepartmentService {

    private static final Logger LOGGER = getLogger(DepartmentService.class);

    private static final String CAREER_NAME = "Career Opportunities";

    private static final String RESEARCH_NAME = "Research Opportunities";

    private static final List<String> CAREER_CATEGORIES = ImmutableList.of("Employment", "Internship", "Volunteering");

    private static final List<String> RESEARCH_CATEGORIES = ImmutableList.of("MRes", "PhD", "Postdoc");

    @Value("${department.draft.expiry.seconds}")
    private Long departmentDraftExpirySeconds;

    @Value("${department.pending.expiry.seconds}")
    private Long departmentPendingExpirySeconds;

    @Value("${department.pending.notification.interval1.seconds}")
    private Long departmentPendingNotificationInterval1Seconds;

    @Value("${department.pending.notification.interval2.seconds}")
    private Long departmentPendingNotificationInterval2Seconds;

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private DepartmentDAO departmentDAO;

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
    private PostService postService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Inject
    private PaymentService paymentService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private EntityManager entityManager;

    public Department getDepartment(Long id) {
        User user = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        return verifyCanView(user, department);
    }

    public Department getDepartment(String handle) {
        User user = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, handle);
        return verifyCanView(user, department);
    }

    public DepartmentDashboard getDepartmentDashboard(Long id) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        List<hr.prism.board.domain.ResourceTask> tasks = resourceTaskService.getTasks(id);
        List<Board> boards = boardService.getBoards(id, true, ACCEPTED, null, null);
        Statistics memberStatistics = userRoleService.getMemberStatistics(id);
        List<Organization> organizations = postService.getOrganizations(id);
        PostStatistics postStatistics = postService.getPostStatistics(id);

        List<Invoice> invoices = null;
        try {
            invoices = getInvoices(id);
        } catch (Throwable t) {
            LOGGER.warn("Cannot get invoices for department ID: " + id, t);
        }

        return
            new DepartmentDashboard()
                .setDepartment(department)
                .setTasks(tasks)
                .setBoards(boards)
                .setMemberStatistics(memberStatistics)
                .setOrganizations(organizations)
                .setPostStatistics(postStatistics)
                .setInvoices(invoices);
    }

    public List<Department> getDepartments(Boolean includePublicDepartments, String searchTerm) {
        User user = userService.getCurrentUser();
        List<Resource> resources =
            resourceService.getResources(user,
                new ResourceFilter()
                    .setScope(DEPARTMENT)
                    .setSearchTerm(searchTerm)
                    .setIncludePublicResources(includePublicDepartments)
                    .setOrderStatement("resource.name"));

        return resources.stream()
            .map(resource -> (Department) resource)
            .collect(toList());
    }

    public Department createDepartment(Long universityId, DepartmentDTO departmentDTO) {
        User user = userService.getCurrentUserSecured();
        University university = universityService.getUniversity(universityId);
        resourceService.validateUniqueName(
            DEPARTMENT, null, university, departmentDTO.getName(), DUPLICATE_DEPARTMENT);
        String name = normalizeSpace(departmentDTO.getName());

        Department department = new Department();
        department.setName(name);
        department.setSummary(departmentDTO.getSummary());

        DocumentDTO documentLogoDTO = departmentDTO.getDocumentLogo();
        if (documentLogoDTO == null) {
            department.setDocumentLogo(university.getDocumentLogo());
        } else {
            department.setDocumentLogo(documentService.getOrCreateDocument(documentLogoDTO));
        }

        department.setHandle(resourceService.createHandle(university, name,
            departmentRepository::findHandleByLikeSuggestedHandle));
        department = departmentRepository.save(department);
        resourceService.updateState(department, DRAFT);

        List<String> memberCategoryStrings;
        List<MemberCategory> memberCategories = departmentDTO.getMemberCategories();
        if (CollectionUtils.isEmpty(memberCategories)) {
            memberCategoryStrings = MEMBER_CATEGORY_STRINGS;
        } else {
            memberCategoryStrings = toStrings(departmentDTO.getMemberCategories());
        }

        resourceService.updateCategories(department, CategoryType.MEMBER, memberCategoryStrings);
        resourceService.createResourceRelation(university, department);
        resourceService.setIndexDataAndQuarter(department);
        resourceService.createResourceOperation(department, EXTEND, user);
        userRoleService.createOrUpdateUserRole(department, user, ADMINISTRATOR);

        // Create the initial boards
        Long departmentId = department.getId();
        boardService.createBoard(departmentId,
            new BoardDTO().setName(CAREER_NAME).setPostCategories(CAREER_CATEGORIES));
        boardService.createBoard(departmentId,
            new BoardDTO().setName(RESEARCH_NAME).setPostCategories(RESEARCH_CATEGORIES));

        // Create the initial tasks
        department.setLastTaskCreationTimestamp(LocalDateTime.now());
        resourceTaskService.createForNewResource(departmentId, user.getId(), DEPARTMENT_TASKS);

        entityManager.refresh(department);
        return (Department) resourceService.getResource(user, DEPARTMENT, departmentId);
    }

    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(currentUser, DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, EDIT, () -> {
            department.setChangeList(new ChangeListRepresentation());
            resourcePatchService.patchName(department,
                departmentDTO.getName(), DUPLICATE_DEPARTMENT);
            resourcePatchService.patchProperty(department, "summary",
                department::getSummary, department::setSummary, departmentDTO.getSummary());
            resourcePatchService.patchHandle(department, departmentDTO.getHandle(), DUPLICATE_DEPARTMENT_HANDLE);
            resourcePatchService.patchDocument(department, "documentLogo",
                department::getDocumentLogo, department::setDocumentLogo, departmentDTO.getDocumentLogo());
            resourcePatchService.patchCategories(department,
                CategoryType.MEMBER, toStrings(departmentDTO.getMemberCategories()));
            departmentRepository.update(department);
            resourceService.setIndexDataAndQuarter(department);
            return department;
        });
    }

    public List<DepartmentSearch> findDepartment(Long universityId, String searchTerm) {
        return departmentDAO.findDepartments(universityId, searchTerm);
    }

    public List<String> findProgramsBySimilarName(Long departmentId, String searchTerm) {
        return departmentDAO.findDepartmentPrograms(departmentId, searchTerm);
    }

    public Department createMembers(Long departmentId, List<UserRoleDTO> userRoleDTOs) {
        if (userRoleDTOs.stream().map(UserRoleDTO::getRole).anyMatch(role -> role != MEMBER)) {
            throw new BoardException(INVALID_RESOURCE_USER, "Only members can be bulk created");
        }

        User currentUser = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(currentUser, DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, EDIT, () -> {
            department.increaseMemberTobeUploadedCount((long) userRoleDTOs.size());
            userRoleEventService.publishEvent(this, currentUser.getId(), departmentId, userRoleDTOs);
            department.setLastMemberTimestamp(LocalDateTime.now());
            return department;
        });
    }

    public User createMembershipRequest(Long departmentId, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured(true);
        Department department = (Department) resourceService.findOne(departmentId);

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, MEMBER);
        if (userRole != null) {
            if (userRole.getState() == REJECTED) {
                // User has been rejected already, don't let them be a nuisance by repeatedly retrying
                throw new BoardForbiddenException(FORBIDDEN_PERMISSION, "User has already been rejected as a member");
            }

            throw new BoardException(DUPLICATE_PERMISSION, "User has already requested membership");
        }


        UserDTO userDTO = userRoleDTO.getUser();
        if (userDTO != null) {
            // We validate the membership later - avoid NPE now
            userService.updateMembershipData(user, userDTO);
        }

        userRoleDTO.setRole(MEMBER);
        userRole = userRoleCacheService.createUserRole(user, department, user, userRoleDTO, PENDING, false);
        validateMembership(user, department, BoardException.class, INVALID_MEMBERSHIP);

        hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
            .setScope(DEPARTMENT).setRole(ADMINISTRATOR).setActivity(JOIN_DEPARTMENT_REQUEST_ACTIVITY);
        activityEventService.publishEvent(this, departmentId, userRole, singletonList(activity));

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(DEPARTMENT).setRole(ADMINISTRATOR).setNotification(JOIN_DEPARTMENT_REQUEST_NOTIFICATION);
        notificationEventService.publishEvent(this, departmentId, singletonList(notification));
        return user;
    }

    public UserRole viewMembershipRequest(Long departmentId, Long userId) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, DEPARTMENT, departmentId);
        actionService.executeAction(user, department, EDIT, () -> department);
        UserRole userRole = userRoleService.findByResourceAndUserIdAndRole(department, userId, MEMBER);
        activityService.viewActivity(userRole.getActivity(), user);
        return userRole.setViewed(true);
    }

    public void reviewMembershipRequest(Long departmentId, Long userId, State state) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, DEPARTMENT, departmentId);
        actionService.executeAction(user, department, EDIT, () -> {
            UserRole userRole = userRoleService.findByResourceAndUserIdAndRole(department, userId, MEMBER);
            if (userRole.getState() == PENDING) {
                userRole.setState(state);
                activityEventService.publishEvent(this, departmentId, userRole);
            }

            return department;
        });
    }

    public User updateMembershipData(Long departmentId, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured(true);
        Department department = (Department) resourceService.findOne(departmentId);

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, MEMBER);
        if (userRole == null || userRole.getState() == REJECTED) {
            throw new BoardForbiddenException(FORBIDDEN_PERMISSION, "User is not a member");
        }

        UserDTO userDTO = userRoleDTO.getUser();
        if (userDTO != null) {
            userService.updateMembershipData(user, userDTO);
        }

        userRoleCacheService.updateMembershipData(userRole, userRoleDTO);
        validateMembership(user, department, BoardException.class, INVALID_MEMBERSHIP);
        return user;
    }

    public void validateMembership(User user, Department department, Class<? extends BoardException> exceptionClass,
                                   ExceptionCode exceptionCode) {
        PostResponseReadinessRepresentation responseReadiness =
            makePostResponseReadiness(user, department, true);
        if (!responseReadiness.isReady()) {
            if (responseReadiness.isRequireUserDemographicData()) {
                throwFor(exceptionClass, exceptionCode, "User demographic data not valid");
            }

            throwFor(exceptionClass, exceptionCode, "User role demographic data not valid");
        }
    }

    public void decrementMemberCountPending(Long departmentId) {
        ((Department) resourceService.findOne(departmentId)).decrementMemberToBeUploadedCount();
    }

    public List<Long> findAllIdsForTaskUpdates(LocalDateTime baseline) {
        LocalDateTime baseline1 = baseline.minusMonths(1);

        LocalDateTime baseline2;
        if (baseline.getMonth().getValue() > 8) {
            baseline2 = LocalDateTime.of(baseline.getYear(), 9, 1, 0, 0);
        } else {
            baseline2 = LocalDateTime.of(baseline.getYear() - 1, 9, 1, 0, 0);
        }

        return departmentRepository.findAllIdsForTaskUpdates(baseline1, baseline2);
    }

    public void updateTasks(Long departmentId, LocalDateTime baseline) {
        List<ResourceTask> tasks = new ArrayList<>();
        Department department = (Department) resourceService.findOne(departmentId);

        LocalDateTime lastMemberTimestamp = department.getLastMemberTimestamp();
        if (lastMemberTimestamp == null || lastMemberTimestamp.isBefore(baseline)) {
            tasks.add(UPDATE_MEMBER);
        }

        department.setLastTaskCreationTimestamp(baseline);
        resourceTaskService.createForExistingResource(departmentId, department.getCreatorId(), tasks);
    }

    public void updateSubscriptions(LocalDateTime baseline) {
        LocalDateTime draftExpiryTimestamp = baseline.minusSeconds(departmentDraftExpirySeconds);
        executeActions(DRAFT, draftExpiryTimestamp, CONVERT, PENDING);

        LocalDateTime pendingExpiryTimestamp = baseline.minusSeconds(departmentPendingExpirySeconds);
        executeActions(PENDING, pendingExpiryTimestamp, REJECT, REJECTED);
    }

    public List<Long> findAllIdsForSubscribeNotification(LocalDateTime baseline) {
        LocalDateTime baseline1 = baseline.minusSeconds(departmentPendingNotificationInterval1Seconds);
        LocalDateTime baseline2 = baseline.minusSeconds(departmentPendingNotificationInterval2Seconds);
        return departmentRepository.findAllIdsForSubscribeNotification(
            PENDING, 1, 2, baseline1, baseline2);
    }

    public void sendSubscribeNotification(Long departmentId) {
        Department department = (Department) resourceService.findOne(departmentId);
        hr.prism.board.domain.Activity subscribeActivity = activityService.findByResourceAndActivityAndRole(
            department, SUBSCRIBE_DEPARTMENT_ACTIVITY, DEPARTMENT, ADMINISTRATOR);
        if (subscribeActivity == null) {
            hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
                .setScope(DEPARTMENT).setRole(ADMINISTRATOR).setActivity(SUBSCRIBE_DEPARTMENT_ACTIVITY);
            activityEventService.publishEvent(this, departmentId, false, singletonList(activity));
        }

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(DEPARTMENT).setRole(ADMINISTRATOR).setNotification(SUBSCRIBE_DEPARTMENT_NOTIFICATION);
        notificationEventService.publishEvent(this, departmentId, singletonList(notification));

        Integer notifiedCount = department.getNotifiedCount();
        department.setNotifiedCount(notifiedCount == null ? 1 : notifiedCount + 1);
    }

    public Customer getPaymentSources(Long departmentId) {
        String customerId = getCustomerIdSecured(departmentId);
        return customerId == null ? null : paymentService.getCustomer(customerId);
    }

    public List<Invoice> getInvoices(Long departmentId) {
        String customerId = getCustomerIdSecured(departmentId);
        if (customerId == null) {
            return null;
        }

        InvoiceCollection invoiceCollection = paymentService.getInvoices(customerId);
        if (invoiceCollection == null) {
            return null;
        }

        return invoiceCollection.getData();
    }

    public Customer addPaymentSourceAndSubscription(Long departmentId, String source) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(departmentId);
        actionService.executeAction(user, department, SUBSCRIBE, () -> {
            Customer customer;
            String customerId = department.getCustomerId();
            if (customerId == null) {
                customer = paymentService.createCustomer(source);
                customerId = customer.getId();
                department.setCustomerId(customerId);
                paymentService.createSubscription(customerId);
            } else {
                customer = paymentService.getCustomer(customerId);
                // Not clear how subscription data is initialized, so code defensively for null pointer risk
                CustomerSubscriptionCollection subscriptions = customer.getSubscriptions();
                if (subscriptions == null || CollectionUtils.isEmpty(customer.getSubscriptions().getData())) {
                    customer = paymentService.createSubscription(customerId);
                } else {
                    customer = paymentService.appendSource(customerId, source);
                }
            }

            department.setCustomer(customer);
            return department;
        });

        return department.getCustomer();
    }

    public Customer setPaymentSourceAsDefault(Long departmentId, String defaultSource) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(departmentId);
        actionService.executeAction(user, department, SUBSCRIBE, () -> {
            String customerId = department.getCustomerId();
            if (customerId != null) {
                Customer customer = paymentService.setSourceAsDefault(customerId, defaultSource);
                department.setCustomer(customer);
            }

            return department;
        });

        return department.getCustomer();
    }

    public Customer deletePaymentSource(Long departmentId, String source) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(departmentId);
        actionService.executeAction(user, department, EDIT, () -> {
            String customerId = department.getCustomerId();
            if (customerId != null) {
                Customer customer = paymentService.deleteSource(customerId, source);
                department.setCustomer(customer);

                ExternalAccountCollection sources = customer.getSources();
                if (sources == null || CollectionUtils.isEmpty(sources.getData())) {
                    actionService.executeAction(user, department, UNSUBSCRIBE, () -> department);
                }
            }

            return department;
        });

        return department.getCustomer();
    }

    public Customer cancelSubscription(Long departmentId) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(departmentId);
        actionService.executeAction(user, department, UNSUBSCRIBE, () -> {
            String customerId = department.getCustomerId();
            if (customerId != null) {
                Customer customer = paymentService.cancelSubscription(customerId);
                department.setCustomer(customer);
            }

            return department;
        });

        return department.getCustomer();
    }

    public Customer reactivateSubscription(Long departmentId) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(departmentId);
        actionService.executeAction(user, department, SUBSCRIBE, () -> {
            String customerId = department.getCustomerId();
            if (customerId != null) {
                Customer customer = paymentService.reactivateSubscription(customerId);
                department.setCustomer(customer);
            }

            return department;
        });

        return department.getCustomer();
    }

    public void processStripeWebhookEvent(String customerId, Action action) {
        Department department = departmentRepository.findByCustomerId(customerId);
        if (department == null) {
            throw new BoardException(PAYMENT_INTEGRATION_ERROR, "No department with customer ID: " + customerId);
        }

        State state;
        switch (action) {
            case SUSPEND:
                state = department.getState();
                break;
            case UNSUBSCRIBE:
                state = REJECTED;
                break;
            default:
                throw new BoardException(PROBLEM, "Unexpected action");
        }

        Long departmentId = department.getId();
        actionService.executeAnonymously(singletonList(departmentId), action, state, LocalDateTime.now());
        entityManager.refresh(department);
        if (action == Action.SUSPEND) {
            hr.prism.board.domain.Activity suspendActivity = activityService.findByResourceAndActivityAndRole(
                department, SUBSCRIBE_DEPARTMENT_ACTIVITY, DEPARTMENT, ADMINISTRATOR);
            if (suspendActivity == null) {
                hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
                    .setScope(DEPARTMENT).setRole(ADMINISTRATOR).setActivity(SUSPEND_DEPARTMENT_ACTIVITY);
                activityEventService.publishEvent(this, departmentId, false, singletonList(activity));
            }

            hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
                .setScope(DEPARTMENT).setRole(ADMINISTRATOR).setNotification(SUSPEND_DEPARTMENT_NOTIFICATION);
            notificationEventService.publishEvent(this, departmentId, singletonList(notification));
        }
    }

    public PostResponseReadinessRepresentation makePostResponseReadiness(User user, Department department,
                                                                         boolean canPursue) {
        PostResponseReadinessRepresentation responseReadiness = new PostResponseReadinessRepresentation();
        if (Stream.of(user.getGender(), user.getAgeRange(), user.getLocationNationality()).anyMatch(Objects::isNull)) {
            // User data incomplete
            responseReadiness.setRequireUserDemographicData(true);
        }

        if (department.getMemberCategories().isEmpty()) {
            return responseReadiness;
        }

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, MEMBER);
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

    private Department verifyCanView(User user, Department department) {
        return (Department) actionService.executeAction(user, department, VIEW, () -> department);
    }

    private String getCustomerIdSecured(Long departmentId) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, departmentId);
        actionService.executeAction(user, department, EDIT, () -> department);
        return department.getCustomerId();
    }

    private void executeActions(State state, LocalDateTime baseline, Action action, State newState) {
        List<Long> departmentIds = departmentRepository.findByStateAndStateChangeTimestampLessThan(state, baseline);
        if (!departmentIds.isEmpty()) {
            actionService.executeAnonymously(departmentIds, action, newState, LocalDateTime.now());
        }
    }

}
