package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.University;
import hr.prism.board.representation.*;
import hr.prism.board.service.ResourceService;
import hr.prism.board.value.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.MemberCategory.fromStrings;
import static java.util.stream.Collectors.toList;

@Component
public class DepartmentMapper implements Function<Department, DepartmentRepresentation> {

    private final DocumentMapper documentMapper;

    private final ResourceMapper resourceMapper;

    private final UniversityMapper universityMapper;

    private final OrganizationMapper organizationMapper;

    private final ResourceService resourceService;

    @Inject
    public DepartmentMapper(DocumentMapper documentMapper, ResourceMapper resourceMapper,
                            UniversityMapper universityMapper, OrganizationMapper organizationMapper,
                            ResourceService resourceService) {
        this.documentMapper = documentMapper;
        this.resourceMapper = resourceMapper;
        this.universityMapper = universityMapper;
        this.organizationMapper = organizationMapper;
        this.resourceService = resourceService;
    }

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
            .setMemberCategories(fromStrings(resourceService.getCategories(department, MEMBER)));
    }

    public DepartmentRepresentation apply(ResourceSearch department) {
        if (department == null) {
            return null;
        }

        return new DepartmentRepresentation()
            .setId(department.getId())
            .setName(department.getName())
            .setDocumentLogo(documentMapper.apply(department));
    }

    public DepartmentDashboardRepresentation apply(DepartmentDashboard departmentDashboard) {
        if (departmentDashboard == null) {
            return null;
        }

        return new DepartmentDashboardRepresentation()
            .setTasks(applyTasks(departmentDashboard.getTasks()))
            .setBoards(applyBoards(departmentDashboard.getDepartment(), departmentDashboard.getBoards()))
            .setMemberStatistics(applyMemberStatistics(departmentDashboard.getMemberStatistics()))
            .setOrganizationStatistics(applyOrganizations(departmentDashboard.getOrganizationStatistics()))
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
            new ResourceTaskRepresentation()
                .setId(task.getId())
                .setTask(task.getTask())
                .setCompleted(task.getCompleted()))
            .collect(toList());
    }

    private List<BoardRepresentation> applyBoards(Department department, List<Board> boards) {
        if (boards == null) {
            return null;
        }

        return boards.stream().map(board ->
            resourceMapper.apply(board, BoardRepresentation.class)
                .setHandle(resourceMapper.getHandle(board, department))
                .setPostCategories(resourceService.getCategories(board, POST)))
            .collect(toList());
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

    private List<OrganizationStatisticsRepresentation> applyOrganizations(List<OrganizationStatistics> organizations) {
        if (organizations == null) {
            return null;
        }

        return organizations.stream().map(organizationMapper::apply).collect(toList());
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
