package hr.prism.board.service;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DepartmentPatchDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.dto.UserRoleDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.NotificationEventService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private static final String SIMILAR_DEPARTMENT =
        "SELECT resource.id, resource.name, document_logo.cloudinary_id, document_logo.cloudinary_url, document_logo.file_name, " +
            "IF(resource.scope = :scope AND resource.state = :state, 1, 0) AS valid, " +
            "IF(resource.name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH resource.name against(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "LEFT JOIN document AS document_logo " +
            "ON resource.document_logo_id = document_logo.id " +
            "HAVING valid = 1 AND (similarityHard = 1 OR similaritySoft > 0) " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.name " +
            "LIMIT 10";

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @Inject
    private DepartmentRepository departmentRepository;

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
    private NotificationEventService notificationEventService;

    @Inject
    private ActivityService activityService;

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
        return (Department) actionService.executeAction(currentUser, department, Action.VIEW, () -> department);
    }

    public List<Department> getDepartments(Boolean includePublicDepartments) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilterDTO()
                .setScope(Scope.DEPARTMENT)
                .setIncludePublicResources(includePublicDepartments)
                .setOrderStatement("order by resource.name"))
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
            resourceService.createResourceOperation(department, Action.EXTEND, currentUser);
            userRoleService.createUserRole(department, currentUser, Role.ADMINISTRATOR);
            return (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, department.getId());
        }
    }

    public Department updateDepartment(Long departmentId, DepartmentPatchDTO departmentDTO) {
        User currentUser = userService.getCurrentUser();
        Department department = (Department) resourceService.getResource(currentUser, Scope.DEPARTMENT, departmentId);
        return (Department) actionService.executeAction(currentUser, department, Action.EDIT, () -> {
            department.setChangeList(new ResourceChangeListRepresentation());
            resourcePatchService.patchName(department, departmentDTO.getName(), ExceptionCode.DUPLICATE_DEPARTMENT);
            resourcePatchService.patchProperty(department, "summary", department::getSummary, department::setSummary, departmentDTO.getSummary());
            resourcePatchService.patchHandle(department, departmentDTO.getHandle(), ExceptionCode.DUPLICATE_DEPARTMENT_HANDLE);
            resourcePatchService.patchDocument(department, "documentLogo", department::getDocumentLogo, department::setDocumentLogo, departmentDTO.getDocumentLogo());
            resourcePatchService.patchCategories(department, CategoryType.MEMBER, MemberCategory.toStrings(departmentDTO.getMemberCategories()));
            departmentRepository.update(department);
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
                ((DepartmentRepresentation) new DepartmentRepresentation().setId(Long.parseLong(row[0].toString())).setName(row[1].toString()));
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
        Resource department = resourceService.getResource(user, Scope.DEPARTMENT, departmentId);

        List<ActionRepresentation> actions = department.getActions();
        if (actions != null && actions.stream().anyMatch(action -> action.getAction() == Action.PURSUE)) {
            // User can already do what they want to do, membership would be redundant
            throw new BoardException(ExceptionCode.DUPLICATE_PERMISSION);
        }

        UserRole userRole = userRoleService.findByResourceAndUserAndRoleAndState(department, user, Role.MEMBER, State.REJECTED);
        if (userRole != null) {
            // User has been rejected already, don't let them be a nuisance by repeatedly retrying
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PERMISSION);
        }

        userRoleDTO.setRole(Role.MEMBER);
        userRoleCacheService.createUserRole(user, department, user, userRoleDTO, State.PENDING, false);

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setNotification(Notification.JOIN_DEPARTMENT_REQUEST);
        notificationEventService.publishEvent(this, departmentId, Collections.singletonList(notification));
        activityService.getOrCreateActivity(userRole, Scope.DEPARTMENT, Role.ADMINISTRATOR);
    }

    public void processMembershipRequest(Long departmentId, Long userId, State state) {
        User user = userService.getCurrentUserSecured();
        Resource department = resourceService.getResource(user, Scope.DEPARTMENT, departmentId);
        actionService.executeAction(user, department, Action.EDIT, () -> {
            UserRole userRole = userRoleService.findByResourceAndUserIdAndRole(department, userId, Role.MEMBER);
            userRole.setState(state);
            activityService.deleteActivities(userRole);
            return department;
        });
    }

}
