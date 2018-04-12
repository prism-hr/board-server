package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.University;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.*;
import hr.prism.board.service.ResourceService;
import hr.prism.board.value.DepartmentDashboard;
import hr.prism.board.value.Organization;
import hr.prism.board.value.PostStatistics;
import hr.prism.board.value.Statistics;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
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
    private OrganizationMapper organizationMapper;

    @Inject
    private ResourceService resourceService;

    @Override
    public DepartmentRepresentation apply(Department department) {
        if (department == null) {
            return null;
        }

        University university = (University) department.getParent();
        return resourceMapper.apply(department, DepartmentRepresentation.class)
            .setSummary(department.getSummary())
            .setUniversity(universityMapper.apply(university))
            .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
            .setHandle(resourceMapper.getHandle(department, university))
            .setCustomerId(department.getCustomerId())
            .setMemberCategories(MemberCategory.fromStrings(resourceService.getCategories(department, CategoryType.MEMBER)));
    }

    public DepartmentDashboardRepresentation apply(DepartmentDashboard departmentDashboard) {
        if (departmentDashboard == null) {
            return null;
        }

        return new DepartmentDashboardRepresentation()
            .setTasks(applyTasks(departmentDashboard.getTasks()))
            .setBoards(applyBoards(departmentDashboard.getDepartment(), departmentDashboard.getBoards()))
            .setMemberStatistics(applyMemberStatistics(departmentDashboard.getMemberStatistics()))
            .setOrganizations(applyOrganizations(departmentDashboard.getOrganizations()))
            .setInvoices(departmentDashboard.getInvoices())
            .setPostStatistics(applyPostStatistics(departmentDashboard.getPostStatistics()));
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

    private List<ResourceTaskRepresentation> applyTasks(List<ResourceTask> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream().map(task ->
            new ResourceTaskRepresentation().setId(task.getId()).setTask(task.getTask()).setCompleted(task.getCompleted()))
            .collect(Collectors.toList());
    }

    private List<BoardRepresentation> applyBoards(Department department, List<Board> boards) {
        if (boards == null) {
            return null;
        }

        return boards.stream().map(board ->
            resourceMapper.apply(board, BoardRepresentation.class)
                .setHandle(resourceMapper.getHandle(board, department))
                .setPostCategories(resourceService.getCategories(board, CategoryType.POST)))
            .collect(Collectors.toList());
    }

    private StatisticsRepresentation applyMemberStatistics(Statistics memberStatistics) {
        if (memberStatistics == null) {
            return null;
        }

        return new StatisticsRepresentation()
            .setCountLive(memberStatistics.getCountLive())
            .setCountThisYear(memberStatistics.getCountThisYear())
            .setCountAllTime(memberStatistics.getCountAllTime())
            .setMostRecent(memberStatistics.getMostRecent());
    }

    private List<OrganizationRepresentation> applyOrganizations(List<Organization> organizations) {
        if (organizations == null) {
            return null;
        }

        return organizations.stream().map(organizationMapper).collect(Collectors.toList());
    }

    private PostStatisticsRepresentation applyPostStatistics(PostStatistics postStatistics) {
        if (postStatistics == null) {
            return null;
        }

        return new PostStatisticsRepresentation()
            .setCountLive(postStatistics.getCountLive())
            .setCountThisYear(postStatistics.getCountThisYear())
            .setCountAllTime(postStatistics.getCountAllTime())
            .setMostRecent(postStatistics.getMostRecent())
            .setViewCountLive(postStatistics.getViewCountLive())
            .setViewCountThisYear(postStatistics.getViewCountThisYear())
            .setViewCountAllTime(postStatistics.getViewCountAllTime())
            .setMostRecentView(postStatistics.getMostRecentView())
            .setReferralCountLive(postStatistics.getReferralCountLive())
            .setReferralCountThisYear(postStatistics.getReferralCountThisYear())
            .setReferralCountAllTime(postStatistics.getReferralCountAllTime())
            .setMostRecentReferral(postStatistics.getMostRecentReferral())
            .setResponseCountLive(postStatistics.getResponseCountLive())
            .setResponseCountThisYear(postStatistics.getResponseCountThisYear())
            .setResponseCountAllTime(postStatistics.getResponseCountAllTime())
            .setMostRecentResponse(postStatistics.getMostRecentResponse());
    }

}
