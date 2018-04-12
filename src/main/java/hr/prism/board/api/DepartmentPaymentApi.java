package hr.prism.board.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import hr.prism.board.service.DepartmentService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class DepartmentPaymentApi {

    private final DepartmentService departmentService;

    private final ObjectMapper objectMapper;

    @Inject
    public DepartmentPaymentApi(DepartmentService departmentService, ObjectMapper objectMapper) {
        this.departmentService = departmentService;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources", method = GET)
    public JsonNode getPaymentSources(@PathVariable Long departmentId) throws IOException {
        Customer customer = departmentService.getPaymentSources(departmentId);
        return readTree(customer);
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = POST)
    public JsonNode addPaymentSourceAndSubscription(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentService.addPaymentSourceAndSubscription(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}/setDefault", method = POST)
    public JsonNode setPaymentSourceAsDefault(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentService.setPaymentSourceAsDefault(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/paymentSources/{source}", method = DELETE)
    public JsonNode deletePaymentSource(@PathVariable Long departmentId, @PathVariable String source)
        throws IOException {
        return readTree(departmentService.deletePaymentSource(departmentId, source));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/cancelSubscription", method = POST)
    public JsonNode cancelSubscription(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentService.cancelSubscription(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/reactivateSubscription", method = POST)
    public JsonNode reactivateSubscription(@PathVariable Long departmentId) throws IOException {
        return readTree(departmentService.reactivateSubscription(departmentId));
    }

    @RequestMapping(value = "/api/departments/{departmentId}/invoices", method = GET)
    public List<Invoice> getInvoices(@PathVariable Long departmentId) {
        return departmentService.getInvoices(departmentId);
    }

    private JsonNode readTree(StripeObject object) throws IOException {
        return object == null ? null : objectMapper.readTree(object.toJson());
    }

}
