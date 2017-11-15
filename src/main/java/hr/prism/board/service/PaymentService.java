package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void postConstruct() {
        Stripe.apiKey = stripeApiKey;
    }

    Customer getCustomer(String customerId) {
        try {
            return Customer.retrieve(customerId);
        } catch (Exception e) {
            throw new BoardException(ExceptionCode.PAYMENT_INTEGRATION_ERROR, "Could not get customer with id: " + customerId, e);
        }
    }

    Customer createCustomer(String source) {
        try {
            return Customer.create(ImmutableMap.of("source", source));
        } catch (Exception e) {
            throw new BoardException(ExceptionCode.PAYMENT_INTEGRATION_ERROR, "Could not create customer with source: " + source, e);
        }
    }

    Customer updateCustomer(String customerId, String source) {
        try {
            return Customer.retrieve(customerId).update(ImmutableMap.of("source", source));
        } catch (Exception e) {
            throw new BoardException(ExceptionCode.PAYMENT_INTEGRATION_ERROR, "Could not update customer with id: " + customerId + " and source: " + source, e);
        }
    }

}
