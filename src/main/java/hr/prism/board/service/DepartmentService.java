package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private static final String SIMILAR_DEPARTMENT =
        "SELECT resource.id, resource.name, document_logo.cloudinary_id, document_logo.cloudinary_url, document_logo.file_name, " +
            "IF(resource.name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH resource.name against(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "LEFT JOIN document AS document_logo " +
            "ON resource.document_logo_id = document_logo.id " +
            "WHERE resource.scope = :scope AND resource.state = :state " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.name " +
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

            String handle = ResourceService.suggestHandle(name);
            List<String> similarHandles = departmentRepository.findHandleByLikeSuggestedHandle(handle);
            handle = ResourceService.confirmHandle(handle, similarHandles);
            resourceService.updateHandle(department, handle);
            department = departmentRepository.save(department);

            resourceService.updateCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(departmentDTO.getMemberCategories()));
            resourceService.createResourceRelation(department, department);
            resourceService.setIndexDataAndQuarter(department);
            resourceService.createResourceOperation(department, Action.EXTEND, currentUser);
            userRoleService.createOrUpdateUserRole(department, currentUser, Role.ADMINISTRATOR);
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
        }
    }

    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUser();
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

    public void createMembershipRequest(Long departmentId, UserRoleDTO userRoleDTO) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.findOne(departmentId);

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(department, user, Role.MEMBER);
        if (userRole != null) {
            if (userRole.getState() == State.REJECTED) {
                // User has been rejected already, don't let them be a nuisance by repeatedly retrying
                throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PERMISSION);
            }

            throw new BoardException(ExceptionCode.DUPLICATE_PERMISSION);
        }

        userRoleDTO.setRole(Role.MEMBER);
        userRole = userRoleCacheService.createUserRole(user, department, user, userRoleDTO, State.PENDING, false);

        hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
            .setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setActivity(hr.prism.board.enums.Activity.JOIN_DEPARTMENT_REQUEST_ACTIVITY);
        activityEventService.publishEvent(this, departmentId, userRole, Collections.singletonList(activity));

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(Scope.DEPARTMENT).setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setNotification(Notification.JOIN_DEPARTMENT_REQUEST_NOTIFICATION);
        notificationEventService.publishEvent(this, departmentId, Collections.singletonList(notification));
    }

    @SuppressWarnings("JpaQlInspection")
    public List<UserRole> getMembershipRequests(Long departmentId, String searchTerm) {
        User user = userService.getCurrentUserSecured();
        Resource department = getDepartment(departmentId);
        actionService.executeAction(user, department, Action.EDIT, () -> department);

        List<Long> userIds = userService.findByResourceAndState(department, State.PENDING);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        String search = UUID.randomUUID().toString();
        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            userService.createSearchResults(search, searchTerm, userIds);
            entityManager.flush();
        }

        List<hr.prism.board.domain.UserRole> userRoles = new TransactionTemplate(platformTransactionManager).execute(status -> {
            String statement =
                "select distinct userRole " +
                    "from UserRole userRole " +
                    "inner join userRole.user user " +
                    "left join user.searches search on search.search = :search " +
                    "where user.id in (:userIds) ";
            if (searchTermApplied) {
                statement += "and search.id is not null ";
            }

            statement += "order by search.id, user.id desc";
            return entityManager.createQuery(statement, UserRole.class)
                .setParameter("search", search)
                .setParameter("userIds", userIds)
                .getResultList();
        });

        if (searchTermApplied) {
            userService.deleteSearchResults(search);
        }

        Map<hr.prism.board.domain.Activity, UserRole> indexByActivities = userRoles.stream().collect(Collectors.toMap(UserRole::getActivity, userRole -> userRole));
        for (hr.prism.board.domain.ActivityEvent activityEvent : activityService.findViews(indexByActivities.keySet(), user)) {
            indexByActivities.get(activityEvent.getActivity()).setViewed(true);
        }

        return userRoles;
    }

    public UserRole viewMembershipRequest(Long departmentId, Long userId) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        actionService.executeAction(user, department, Action.EDIT, () -> department);
        UserRole userRole = userRoleService.findByResourceAndUserIdAndRole(department, userId, Role.MEMBER);
        activityService.viewActivity(userRole.getActivity(), user);
        return userRole.setViewed(true);
    }

    public void processMembershipRequest(Long departmentId, Long userId, State state) {
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

}
