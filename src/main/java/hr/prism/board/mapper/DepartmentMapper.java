package hr.prism.board.mapper;

import hr.prism.board.domain.Department;
import hr.prism.board.domain.Department.DepartmentDashboard;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.University;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceTaskRepresentation;
import hr.prism.board.service.ResourceService;
import org.apache.commons.lang3.BooleanUtils;
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
    private BoardMapper boardMapper;

    @Inject
    private ResourceService resourceService;

    @Override
    public DepartmentRepresentation apply(Department department) {
        if (department == null) {
            return null;
        }

        University university = (University) department.getParent();
        return applyDashboard(
            department,
            resourceMapper.apply(department, DepartmentRepresentation.class)
                .setSummary(department.getSummary())
                .setUniversity(universityMapper.apply(university))
                .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
                .setHandle(resourceMapper.getHandle(department, university))
                .setCustomerId(department.getCustomerId())
                .setMemberCategories(MemberCategory.fromStrings(resourceService.getCategories(department, CategoryType.MEMBER))));
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

    private DepartmentRepresentation applyDashboard(Department department, DepartmentRepresentation representation) {
        DepartmentDashboard dashboard = department.getDashboard();
        if (dashboard == null) {
            return representation;
        }

        representation.setPostCount(dashboard.getPostCount());
        representation.setPostCountAllTime(dashboard.getPostCountAllTime());
        representation.setMostRecentPost(dashboard.getMostRecentPost());

        representation.setMemberCount(dashboard.getMemberCount());
        representation.setMemberCountAllTime(dashboard.getMemberCountAllTime());
        representation.setMostRecentMember(dashboard.getMostRecentMember());

        representation.setTasks(dashboard.getTasks().stream().map(this::applyTask).collect(Collectors.toList()));
        representation.setBoards(dashboard.getBoards().stream().map(boardMapper).collect(Collectors.toList()));
        representation.setOrganizations(dashboard.getOrganizations());
        representation.setInvoices(dashboard.getInvoices());

        return representation;
    }

    private ResourceTaskRepresentation applyTask(ResourceTask task) {
        return new ResourceTaskRepresentation().setId(task.getId()).setTask(task.getTask())
            .setCompleted(BooleanUtils.isTrue(task.getCompleted()) || BooleanUtils.isTrue(task.getCompletedForUser()));
    }

}
