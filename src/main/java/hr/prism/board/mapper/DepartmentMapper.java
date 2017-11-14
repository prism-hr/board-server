package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.University;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.DepartmentSubscriptionRepresentation;
import hr.prism.board.service.ActionService;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class DepartmentMapper implements Function<Department, DepartmentRepresentation> {

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private UniversityMapper universityMapper;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActionService actionService;

    @Override
    public DepartmentRepresentation apply(Department department) {
        if (department == null) {
            return null;
        }

        University university = (University) department.getParent();
        return resourceMapper.apply(department, DepartmentRepresentation.class)
            .setUniversity(universityMapper.apply(university))
            .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
            .setHandle(resourceMapper.getHandle(department, university))
            .setSubscription(applySubscription(department))
            .setBoardCount(department.getBoardCount())
            .setMemberCount(department.getMemberCount())
            .setMemberCategories(MemberCategory.fromStrings(resourceService.getCategories(department, CategoryType.MEMBER)))
            .setTasks(department.getTasks().stream().map(ResourceTask::getTask).collect(Collectors.toList()));
    }

    DepartmentRepresentation applySmall(Department department) {
        if (department == null) {
            return null;
        }

        University university = (University) department.getParent();
        return resourceMapper.applySmall(department, DepartmentRepresentation.class)
            .setUniversity(universityMapper.apply(university))
            .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
            .setHandle(resourceMapper.getHandle(department, university));
    }

    private DepartmentSubscriptionRepresentation applySubscription(Department department) {
        if (actionService.canExecuteAction(department, Action.EDIT)) {
            String customerId = department.getCustomerId();
            if (customerId != null) {
                return new DepartmentSubscriptionRepresentation().setCustomerId(customerId).setSubscriptionId(department.getSubscriptionId());
            }
        }

        return null;
    }

}
