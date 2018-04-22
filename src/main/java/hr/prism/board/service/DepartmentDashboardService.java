package hr.prism.board.service;

import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.domain.User;
import hr.prism.board.value.DepartmentDashboard;
import hr.prism.board.value.OrganizationStatistics;
import hr.prism.board.value.PostStatistics;
import hr.prism.board.value.Statistics;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class DepartmentDashboardService {

    private static final Logger LOGGER = getLogger(DepartmentDashboardService.class);

    private final NewUserService userService;

    private final ResourceService resourceService;

    private final ActionService actionService;

    private final ResourceTaskService resourceTaskService;

    private final BoardService boardService;

    private final NewUserRoleService userRoleService;

    private final PostService postService;

    private final PaymentService paymentService;

    private final OrganizationService organizationService;

    @Inject
    public DepartmentDashboardService(NewUserService userService, ResourceService resourceService,
                                      ActionService actionService, ResourceTaskService resourceTaskService,
                                      BoardService boardService, NewUserRoleService userRoleService,
                                      PostService postService, PaymentService paymentService,
                                      OrganizationService organizationService) {
        this.userService = userService;
        this.resourceService = resourceService;
        this.actionService = actionService;
        this.resourceTaskService = resourceTaskService;
        this.boardService = boardService;
        this.userRoleService = userRoleService;
        this.postService = postService;
        this.paymentService = paymentService;
        this.organizationService = organizationService;
    }

    public DepartmentDashboard getDepartmentDashboard(Long id) {
        User user = userService.requireAuthenticatedUser();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);

        List<ResourceTask> tasks = resourceTaskService.getTasks(id);
        List<Board> boards = boardService.getBoards(id, true, ACCEPTED, null, null);
        Statistics memberStatistics = userRoleService.getMemberStatistics(id);
        List<OrganizationStatistics> organizationStatistics = organizationService.getOrganizationStatistics(id);
        PostStatistics postStatistics = postService.getPostStatistics(id);

        List<Invoice> invoices = null;
        try {
            String customerId = department.getCustomerId();
            if (customerId != null) {
                InvoiceCollection invoiceCollection = paymentService.getInvoices(customerId);
                invoices = invoiceCollection == null ? null : invoiceCollection.getData();
            }
        } catch (Throwable t) {
            LOGGER.warn("Could not get invoices for department ID: " + id, t);
        }

        return
            new DepartmentDashboard()
                .setDepartment(department)
                .setTasks(tasks)
                .setBoards(boards)
                .setMemberStatistics(memberStatistics)
                .setOrganizationStatistics(organizationStatistics)
                .setPostStatistics(postStatistics)
                .setInvoices(invoices);
    }

}
