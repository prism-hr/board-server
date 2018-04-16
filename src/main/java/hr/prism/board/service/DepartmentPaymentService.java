package hr.prism.board.service;

import com.stripe.model.Customer;
import com.stripe.model.CustomerSubscriptionCollection;
import com.stripe.model.ExternalAccountCollection;
import com.stripe.model.InvoiceCollection;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.repository.DepartmentRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Activity.SUBSCRIBE_DEPARTMENT_ACTIVITY;
import static hr.prism.board.enums.Activity.SUSPEND_DEPARTMENT_ACTIVITY;
import static hr.prism.board.enums.Notification.SUSPEND_DEPARTMENT_NOTIFICATION;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.REJECTED;
import static hr.prism.board.exception.ExceptionCode.PAYMENT_INTEGRATION_ERROR;
import static java.util.Collections.singletonList;

@Service
@Transactional
public class DepartmentPaymentService {

    private final DepartmentRepository departmentRepository;

    private final ActionService actionService;

    private final PaymentService paymentService;

    private final ResourceService resourceService;

    private final UserService userService;

    private final ActivityService activityService;

    private final EventProducer eventProducer;

    private final EntityManager entityManager;

    public DepartmentPaymentService(DepartmentRepository departmentRepository, ActionService actionService,
                                    PaymentService paymentService, ResourceService resourceService,
                                    UserService userService, ActivityService activityService,
                                    EventProducer eventProducer, EntityManager entityManager) {
        this.departmentRepository = departmentRepository;
        this.actionService = actionService;
        this.paymentService = paymentService;
        this.resourceService = resourceService;
        this.userService = userService;
        this.activityService = activityService;
        this.eventProducer = eventProducer;
        this.entityManager = entityManager;
    }

    public Customer getPaymentSources(Long id) {
        String customerId = getCustomerIdSecured(id);
        return customerId == null ? null : paymentService.getCustomer(customerId);
    }

    public InvoiceCollection getInvoices(Long id) {
        String customerId = getCustomerIdSecured(id);
        return customerId == null ? null : paymentService.getInvoices(customerId);
    }

    public Customer addPaymentSourceAndSubscription(Long id, String source) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(id, user);
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

    public Customer setPaymentSourceAsDefault(Long id, String defaultSource) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(id, user);
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

    public Customer deletePaymentSource(Long id, String source) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(id, user);
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

    public Customer cancelSubscription(Long id) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(id, user);
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

    public Customer reactivateSubscription(Long id) {
        User user = userService.getCurrentUserSecured();
        Department department = getDepartment(id, user);
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

    public void processSubscriptionSuspension(String customerId) {
        Department department = getDepartmentByCustomerIdSecured(customerId);
        Long departmentId = department.getId();

        actionService.executeAnonymously(
            singletonList(departmentId), SUSPEND, department.getState(), LocalDateTime.now());
        entityManager.refresh(department);

        hr.prism.board.domain.Activity suspendActivity = activityService.findByResourceAndActivityAndRole(
            department, SUBSCRIBE_DEPARTMENT_ACTIVITY, DEPARTMENT, ADMINISTRATOR);
        if (suspendActivity == null) {
            eventProducer.produce(
                new ActivityEvent(this, departmentId, false,
                    singletonList(
                        new hr.prism.board.workflow.Activity()
                            .setScope(DEPARTMENT)
                            .setRole(ADMINISTRATOR)
                            .setActivity(SUSPEND_DEPARTMENT_ACTIVITY))));
        }

        eventProducer.produce(
            new NotificationEvent(this, departmentId,
                singletonList(
                    new hr.prism.board.workflow.Notification()
                        .setScope(DEPARTMENT)
                        .setRole(ADMINISTRATOR)
                        .setNotification(SUSPEND_DEPARTMENT_NOTIFICATION))));
    }

    public void processSubscriptionCancellation(String customerId) {
        Department department = getDepartmentByCustomerIdSecured(customerId);
        Long departmentId = department.getId();
        actionService.executeAnonymously(singletonList(departmentId), UNSUBSCRIBE, REJECTED, LocalDateTime.now());
    }

    private Department getDepartment(Long id, User user) {
        return (Department) resourceService.getResource(user, DEPARTMENT, id);
    }

    private String getCustomerIdSecured(Long id) {
        User user = userService.getCurrentUserSecured();
        Department department = (Department) resourceService.getResource(user, DEPARTMENT, id);
        actionService.executeAction(user, department, EDIT, () -> department);
        return department.getCustomerId();
    }

    private Department getDepartmentByCustomerIdSecured(String customerId) {
        Department department = departmentRepository.findByCustomerId(customerId);
        if (department == null) {
            throw new BoardException(PAYMENT_INTEGRATION_ERROR, "No department with customer ID: " + customerId);
        }

        return department;
    }

}
