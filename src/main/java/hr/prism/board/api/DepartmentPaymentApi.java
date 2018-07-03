package hr.prism.board.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Customer;
import com.stripe.model.StripeObject;
import hr.prism.board.domain.User;
import hr.prism.board.service.DepartmentPaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DepartmentPaymentApi {

    private final DepartmentPaymentService departmentPaymentService;

    private final ObjectMapper objectMapper;

    @Inject
    public DepartmentPaymentApi(DepartmentPaymentService departmentPaymentService, ObjectMapper objectMapper) {
        this.departmentPaymentService = departmentPaymentService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources", method = GET)
    public JsonNode getPaymentSources(@AuthenticationPrincipal User user, @PathVariable Long departmentId)
        throws IOException {
        Customer customer = departmentPaymentService.getPaymentSources(user, departmentId);
        return readTree(customer);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = POST)
    public JsonNode addPaymentSourceAndSubscription(@AuthenticationPrincipal User user, @PathVariable Long departmentId,
                                                    @PathVariable String source) throws IOException {
        return readTree(departmentPaymentService.addPaymentSourceAndSubscription(user, departmentId, source));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}/setDefault", method = POST)
    public JsonNode setPaymentSourceAsDefault(@AuthenticationPrincipal User user, @PathVariable Long departmentId,
                                              @PathVariable String source) throws IOException {
        return readTree(departmentPaymentService.setPaymentSourceAsDefault(user, departmentId, source));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = DELETE)
    public JsonNode deletePaymentSource(@AuthenticationPrincipal User user, @PathVariable Long departmentId,
                                        @PathVariable String source) throws IOException {
        return readTree(departmentPaymentService.deletePaymentSource(user, departmentId, source));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/cancelSubscription", method = POST)
    public JsonNode cancelSubscription(@AuthenticationPrincipal User user, @PathVariable Long departmentId)
        throws IOException {
        return readTree(departmentPaymentService.cancelSubscription(user, departmentId));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/reactivateSubscription", method = POST)
    public JsonNode reactivateSubscription(@AuthenticationPrincipal User user, @PathVariable Long departmentId)
        throws IOException {
        return readTree(departmentPaymentService.reactivateSubscription(user, departmentId));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/departments/{departmentId}/invoices", method = GET)
    public JsonNode getInvoices(@AuthenticationPrincipal User user, @PathVariable Long departmentId)
        throws IOException {
        return readTree(departmentPaymentService.getInvoices(user, departmentId));
    }

    private JsonNode readTree(StripeObject object) throws IOException {
        return object == null ? null : objectMapper.readTree(object.toJson());
    }

}
