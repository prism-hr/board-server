package hr.prism.board.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Customer;
import com.stripe.model.StripeObject;
import hr.prism.board.service.DepartmentPaymentService;
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

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources", method = GET)
    public JsonNode getPaymentSources(@PathVariable Long departmentId) throws IOException {
        Customer customer = departmentPaymentService.getPaymentSources(departmentId);
        return readTree(customer);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = POST)
    public JsonNode addPaymentSourceAndSubscription(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentPaymentService.addPaymentSourceAndSubscription(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}/setDefault", method = POST)
    public JsonNode setPaymentSourceAsDefault(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentPaymentService.setPaymentSourceAsDefault(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = DELETE)
    public JsonNode deletePaymentSource(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentPaymentService.deletePaymentSource(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/cancelSubscription", method = POST)
    public JsonNode cancelSubscription(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentPaymentService.cancelSubscription(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/reactivateSubscription", method = POST)
    public JsonNode reactivateSubscription(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentPaymentService.reactivateSubscription(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/invoices", method = GET)
    public JsonNode getInvoices(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentPaymentService.getInvoices(departmentId));
    }

    private JsonNode readTree(StripeObject object) throws IOException {
        return object == null ? null : objectMapper.readTree(object.toJson());
    }

}
