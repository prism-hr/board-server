package hr.prism.board.mapper;

import hr.prism.board.domain.*;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentDashboardRepresentation;
import hr.prism.board.representation.DepartmentDashboardRepresentation.OrganizationRepresentation;
import hr.prism.board.representation.DepartmentDashboardRepresentation.PostStatisticsRepresentation;
import hr.prism.board.representation.DepartmentDashboardRepresentation.StatisticsRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.ResourceTaskRepresentation;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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

    private List<ResourceTaskRepresentation> applyTasks(Set<ResourceTask> tasks) {
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

    private StatisticsRepresentation applyMemberStatistics(Object[] memberStatistics) {
        if (memberStatistics == null) {
            return null;
        }

        return new StatisticsRepresentation()
            .setCountLive((Long) memberStatistics[0])
            .setCountAllTime((Long) memberStatistics[1])
            .setMostRecent((LocalDateTime) memberStatistics[2]);
    }

    private List<OrganizationRepresentation> applyOrganizations(List<Object[]> organizations) {
        if (organizations == null) {
            return null;
        }

        return organizations.stream().map(row ->
            new OrganizationRepresentation().setOrganizationName((String) row[0]).setOrganizationLogo((String) row[1])
                .setPostCount((Long) row[2]).setMostRecentPost((LocalDateTime) row[3]).setPostViewCount((Long) row[4])
                .setPostReferralCount((Long) row[5]).setPostResponseCount((Long) row[6]))
            .collect(Collectors.toList());
    }

    private PostStatisticsRepresentation applyPostStatistics(Object[] postStatistics) {
        if (postStatistics == null) {
            return null;
        }

        return new PostStatisticsRepresentation()
            .setCountLive((Long) postStatistics[0])
            .setCountAllTime((Long) postStatistics[1])
            .setMostRecent((LocalDateTime) postStatistics[2])
            .setCountThisYear((Long) postStatistics[3])
            .setViewCountThisYear((Long) postStatistics[4])
            .setReferralCountThisYear((Long) postStatistics[5])
            .setResponseCountThisYear((Long) postStatistics[6])
            .setViewCountAllTime((Long) postStatistics[7])
            .setReferralCountAllTime((Long) postStatistics[8])
            .setResponseCountAllTime((Long) postStatistics[9])
            .setMostRecentView((LocalDateTime) postStatistics[10])
            .setMostRecentReferral((LocalDateTime) postStatistics[11])
            .setMostRecentResponse((LocalDateTime) postStatistics[12]);
    }

}
