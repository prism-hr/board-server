package hr.prism.board.api;

import com.google.common.base.Charsets;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.DepartmentService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class WebhookApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookApi.class);

    @Value("${stripe.api.event.secret}")
    private String stripeApiEventSecret;

    @Inject
    private DepartmentService departmentService;

    @RequestMapping(value = "/api/webhooks/stripe", method = RequestMethod.POST, consumes = "application/json")
    public void postStripeEvent(HttpServletRequest request) throws SignatureVerificationException, IOException {
        String stripeHeader = request.getHeader("Stripe-Signature");
        String payload = IOUtils.toString(request.getInputStream(), Charsets.UTF_8);
        Event event = Webhook.constructEvent(payload, stripeHeader, stripeApiEventSecret);

        String eventType = event.getType();
        if ("invoice.payment_failed".equals(eventType)) {
            String customerId = ((Invoice) event.getData().getObject()).getCustomer();
            processStripeEvent(customerId, eventType, Action.SUSPEND);
        } else if ("customer.subscription.deleted".equals(eventType)) {
            String customerId = ((Customer) event.getData().getObject()).getId();
            processStripeEvent(customerId, eventType, Action.UNSUBSCRIBE);
        } else if ("customer.subscription.updated".equals(eventType)) {
            Object subscriptionId = ((Subscription)event.getData().getObject()).getId();
            LOGGER.info("Subscription " + subscriptionId + " updated");
        } else {
            throw new BoardException(ExceptionCode.PAYMENT_INTEGRATION_ERROR, "Event of type: " + eventType + " not expected");
        }


    }

    private void processStripeEvent(String customerId, String eventType, Action action) {
        LOGGER.info("Processing event of type: " + eventType + " for customer ID: " + customerId);
        departmentService.processStripeWebhookEvent(customerId, action);
    }

}
