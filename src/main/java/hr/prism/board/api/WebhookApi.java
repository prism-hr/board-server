package hr.prism.board.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@RestController
public class WebhookApi {

    @Value("${stripe.api.event.secret}")
    private String stripeApiEventSecret;

    @Inject
    private DepartmentService departmentService;

    @RequestMapping(value = "/api/webhooks/stripe", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void postStripeEvent(HttpServletRequest request, @RequestBody String payload) throws SignatureVerificationException {
        String stripeHeader = request.getHeader("Stripe-Signature");
        Event event = Webhook.constructEvent(payload, stripeHeader, stripeApiEventSecret);

        String customerId;
        String eventType = event.getType();
        if ("charge.failed".equals(eventType)) {
            customerId = ((Charge) event.getData().getObject()).getCustomer();
        } else if ("customer.subscription.deleted".equals(eventType)) {
            customerId = ((Customer) event.getData().getObject()).getId();
        } else {
            throw new BoardException(ExceptionCode.PAYMENT_INTEGRATION_ERROR, "Event of type: " + eventType + " not expected");
        }

        departmentService.processStripeWebhookEvent(customerId, eventType);
    }

}