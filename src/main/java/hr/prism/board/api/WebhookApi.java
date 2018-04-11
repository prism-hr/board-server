package hr.prism.board.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.BoardException;
import hr.prism.board.service.DepartmentService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static hr.prism.board.enums.Action.SUSPEND;
import static hr.prism.board.enums.Action.UNSUBSCRIBE;
import static hr.prism.board.exception.ExceptionCode.PAYMENT_INTEGRATION_ERROR;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class WebhookApi {

    private static final Logger LOGGER = getLogger(WebhookApi.class);

    private final String stripeApiEventSecret;

    private final DepartmentService departmentService;

    @Inject
    public WebhookApi(@Value("${stripe.api.event.secret}") String stripeApiEventSecret,
                      DepartmentService departmentService) {
        this.stripeApiEventSecret = stripeApiEventSecret;
        this.departmentService = departmentService;
    }

    @RequestMapping(value = "/api/webhooks/stripe", method = POST, consumes = "application/json")
    public void postStripeEvent(HttpServletRequest request) throws SignatureVerificationException, IOException {
        String stripeHeader = request.getHeader("Stripe-Signature");
        String payload = IOUtils.toString(request.getInputStream(), UTF_8);
        Event event = Webhook.constructEvent(payload, stripeHeader, stripeApiEventSecret);

        String eventType = event.getType();
        if ("invoice.payment_failed".equals(eventType)) {
            String customerId = ((Invoice) event.getData().getObject()).getCustomer();
            processStripeEvent(customerId, eventType, SUSPEND);
        } else if ("customer.subscription.deleted".equals(eventType)) {
            String customerId = ((Customer) event.getData().getObject()).getId();
            processStripeEvent(customerId, eventType, UNSUBSCRIBE);
        } else {
            throw new BoardException(PAYMENT_INTEGRATION_ERROR, "Event of type: " + eventType + " not expected");
        }
    }

    private void processStripeEvent(String customerId, String eventType, Action action) {
        LOGGER.info("Processing event of type: " + eventType + " for customer ID: " + customerId);
        departmentService.processStripeWebhookEvent(customerId, action);
    }

}
