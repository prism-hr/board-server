package hr.prism.board.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WebhookApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookApi.class);

    @Value("${stripe.api.event.secret}")
    private String stripeApiEventSecret;

    // TODO: change the state of the department accordingly
    @RequestMapping(value = "/api/webhooks/stripe", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void postStripeEvent(HttpServletRequest request, @RequestBody String payload) throws SignatureVerificationException {
        String stripeHeader = request.getHeader("Stripe-Signature");
        Event event = Webhook.constructEvent(payload, stripeHeader, stripeApiEventSecret);

        String customerId;
        String eventType = event.getType();
        if ("charge.failed".equals(eventType)) {
            customerId = ((Customer) event.getData().getObject()).getId();
        } else if ("customer.subscription.deleted".equals(eventType)) {
            customerId = ((Charge) event.getData().getObject()).getCustomer();
        } else {
            throw new BoardException(ExceptionCode.PAYMENT_INTEGRATION_ERROR, "Event of type: " + eventType + " not expected");
        }

        LOGGER.info("Received event of type: " + eventType + " for customer: " + customerId);
    }

}
