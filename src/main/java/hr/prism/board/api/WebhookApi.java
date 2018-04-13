package hr.prism.board.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import hr.prism.board.exception.BoardException;
import hr.prism.board.service.DepartmentPaymentService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static hr.prism.board.exception.ExceptionCode.PAYMENT_INTEGRATION_ERROR;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class WebhookApi {

    private static final Logger LOGGER = getLogger(WebhookApi.class);

    private final String stripeApiEventSecret;

    private final DepartmentPaymentService departmentPaymentService;

    @Inject
    public WebhookApi(@Value("${stripe.api.event.secret}") String stripeApiEventSecret,
                      DepartmentPaymentService departmentPaymentService) {
        this.stripeApiEventSecret = stripeApiEventSecret;
        this.departmentPaymentService = departmentPaymentService;
    }

    @RequestMapping(value = "/api/webhooks/stripe", method = POST, consumes = "application/json")
    public void postStripeEvent(HttpServletRequest request) throws SignatureVerificationException, IOException {
        String stripeHeader = request.getHeader("Stripe-Signature");
        String payload = IOUtils.toString(request.getInputStream(), UTF_8);
        Event event = Webhook.constructEvent(payload, stripeHeader, stripeApiEventSecret);

        String eventType = event.getType();
        if ("invoice.payment_failed".equals(eventType)) {
            String customerId = ((Invoice) event.getData().getObject()).getCustomer();
            LOGGER.info("Processing event of type: " + eventType + " for customer ID: " + customerId);
            departmentPaymentService.processSubscriptionSuspension(customerId);
        } else if ("customer.subscription.deleted".equals(eventType)) {
            String customerId = ((Customer) event.getData().getObject()).getId();
            LOGGER.info("Processing event of type: " + eventType + " for customer ID: " + customerId);
            departmentPaymentService.processSubscriptionCancellation(customerId);
        } else {
            throw new BoardException(PAYMENT_INTEGRATION_ERROR, "Event of type: " + eventType + " not expected");
        }
    }

}
