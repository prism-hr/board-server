package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.*;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceSearch;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Activity.SUBSCRIBE_DEPARTMENT_ACTIVITY;
import static hr.prism.board.enums.MemberCategory.MEMBER_CATEGORY_STRINGS;
import static hr.prism.board.enums.MemberCategory.toStrings;
import static hr.prism.board.enums.Notification.SUBSCRIBE_DEPARTMENT_NOTIFICATION;
import static hr.prism.board.enums.ResourceTask.RESOURCE_TASKS;
import static hr.prism.board.enums.ResourceTask.UPDATE_MEMBER;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

@Service
@Transactional
public class DepartmentService {

    private static final String CAREER_NAME = "Career Opportunities";

    private static final String RESEARCH_NAME = "Research Opportunities";

    private static final List<String> CAREER_CATEGORIES = ImmutableList.of("Employment", "Internship", "Volunteering");

    private static final List<String> RESEARCH_CATEGORIES = ImmutableList.of("MRes", "PhD", "Postdoc");

    private final Long departmentDraftExpirySeconds;

    private final Long departmentPendingExpirySeconds;

    private final Long departmentPendingNotificationInterval1Seconds;

    private final Long departmentPendingNotificationInterval2Seconds;

    private final DepartmentRepository departmentRepository;

    private final DepartmentDAO departmentDAO;

    private final UserService userService;

    private final DocumentService documentService;

    private final ResourceService resourceService;

    private final ResourcePatchService<Department> resourcePatchService;

    private final UserRoleService userRoleService;

    private final ActionService actionService;

    private final ActivityService activityService;

    private final UniversityService universityService;

    private final BoardService boardService;

    private final ResourceTaskService resourceTaskService;

    private final EventProducer eventProducer;

    private final EntityManager entityManager;

    @Inject
    public DepartmentService(@Value("${department.draft.expiry.seconds}") Long departmentDraftExpirySeconds,
                             @Value("${department.pending.expiry.seconds}") Long departmentPendingExpirySeconds,
                             @Value("${department.pending.notification.interval1.seconds}") Long departmentPendingNotificationInterval1Seconds,
                             @Value("${department.pending.notification.interval2.seconds}") Long departmentPendingNotificationInterval2Seconds,
                             DepartmentRepository departmentRepository, DepartmentDAO departmentDAO,
                             UserService userService, DocumentService documentService, ResourceService resourceService,
                             ResourcePatchService<Department> resourcePatchService, UserRoleService userRoleService,
                             ActionService actionService, ActivityService activityService,
                             UniversityService universityService, BoardService boardService,
                             ResourceTaskService resourceTaskService, EventProducer eventProducer,
                             EntityManager entityManager) {
        this.departmentDraftExpirySeconds = departmentDraftExpirySeconds;
        this.departmentPendingExpirySeconds = departmentPendingExpirySeconds;
        this.departmentPendingNotificationInterval1Seconds = departmentPendingNotificationInterval1Seconds;
        this.departmentPendingNotificationInterval2Seconds = departmentPendingNotificationInterval2Seconds;
        this.departmentRepository = departmentRepository;
        this.departmentDAO = departmentDAO;
        this.userService = userService;
        this.documentService = documentService;
        this.resourceService = resourceService;
        this.resourcePatchService = resourcePatchService;
        this.userRoleService = userRoleService;
        this.actionService = actionService;
        this.activityService = activityService;
        this.universityService = universityService;
        this.boardService = boardService;
        this.resourceTaskService = resourceTaskService;
        this.eventProducer = eventProducer;
        this.entityManager = entityManager;
    }

    public Department getById(Long id) {
        User user = userService.getUser();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        return verifyCanView(user, department);
    }

    public Department getById(String handle) {
        User user = userService.getUser();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, handle);
        return verifyCanView(user, department);
    }

    public List<Department> getDepartments(Boolean includePublicDepartments, String searchTerm) {
        User user = userService.getUser();
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

    public List<ResourceOperation> getDepartmentOperations(Long id) {
        User user = userService.getUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);
        return resourceService.getResourceOperations(department);
    }

    public Department createDepartment(Long universityId, DepartmentDTO departmentDTO) {
        User user = userService.getUserSecured();
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
        userRoleService.createUserRole(department, user, ADMINISTRATOR);

        // Create the initial boards
        Long departmentId = department.getId();
        boardService.createBoard(departmentId,
            new BoardDTO().setName(CAREER_NAME).setPostCategories(CAREER_CATEGORIES));
        boardService.createBoard(departmentId,
            new BoardDTO().setName(RESEARCH_NAME).setPostCategories(RESEARCH_CATEGORIES));

        // Create the initial tasks
        department.setLastTaskCreationTimestamp(LocalDateTime.now());
        resourceTaskService.createForNewResource(departmentId, user.getId(), RESOURCE_TASKS);

        entityManager.refresh(department);
        return (Department) resourceService.getResource(user, DEPARTMENT, departmentId);
    }

    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getUserSecured();
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
            resourceService.setIndexDataAndQuarter(department);
            return department;
        });
    }

    public List<ResourceSearch> findDepartment(Long universityId, String searchTerm) {
        return departmentDAO.findDepartments(universityId, searchTerm);
    }

    public List<String> findProgramsBySimilarName(Long departmentId, String searchTerm) {
        return departmentDAO.findDepartmentPrograms(departmentId, searchTerm);
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
        Department department = (Department) resourceService.getById(departmentId);

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

    public void sendSubscribeNotification(Long id) {
        Department department = (Department) resourceService.getById(id);
        hr.prism.board.domain.Activity subscribeActivity = activityService.getByResourceActivityAndRole(
            department, SUBSCRIBE_DEPARTMENT_ACTIVITY, DEPARTMENT, ADMINISTRATOR);
        if (subscribeActivity == null) {
            eventProducer.produce(
                new ActivityEvent(this, id,
                    singletonList(
                        new hr.prism.board.workflow.Activity()
                            .setScope(DEPARTMENT)
                            .setRole(ADMINISTRATOR)
                            .setActivity(SUBSCRIBE_DEPARTMENT_ACTIVITY))));
        }

        eventProducer.produce(
            new NotificationEvent(this, id,
                singletonList(
                    new hr.prism.board.workflow.Notification()
                        .setScope(DEPARTMENT)
                        .setRole(ADMINISTRATOR)
                        .setNotification(SUBSCRIBE_DEPARTMENT_NOTIFICATION))));

        Integer notifiedCount = department.getNotifiedCount();
        department.setNotifiedCount(notifiedCount == null ? 1 : notifiedCount + 1);
    }

    private Department verifyCanView(User user, Department department) {
        return (Department) actionService.executeAction(user, department, VIEW, () -> department);
    }

    private void executeActions(State state, LocalDateTime baseline, Action action, State newState) {
        List<Long> departmentIds = departmentRepository.findByStateAndStateChangeTimestampLessThan(state, baseline);
        if (!departmentIds.isEmpty()) {
            actionService.executeAnonymously(departmentIds, action, newState, LocalDateTime.now());
        }
    }

}
