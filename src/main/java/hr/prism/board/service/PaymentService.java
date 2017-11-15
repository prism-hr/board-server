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
        return performStripeOperation(() ->
                Customer.retrieve(customerId),
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not get customer ID: " + customerId);
    }

    Customer createCustomer(String source) {
        return performStripeOperation(() ->
                Customer.create(ImmutableMap.of("source", source)),
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not create customer with source: " + source);
    }

    Customer appendSource(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.getSources().create(ImmutableMap.of("source", source));
                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not update customer ID: " + customerId + " with source: " + source);
    }

    Customer setDefaultSource(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.update(ImmutableMap.of("default_source", source));
                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not not set default source: " + source + " for customer ID: " + customerId);
    }

    Customer deleteSource(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.getSources().retrieve(source).delete();
                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not remove source: " + source + " from customer ID: " + customerId);
    }

    private <T> T performStripeOperation(StripeOperation<T> operation, ExceptionCode exceptionCode, String exceptionMessage) {
        try {
            return operation.operate();
        } catch (Exception e) {
            throw new BoardException(exceptionCode, exceptionMessage);
        }
    }

    private interface StripeOperation<T> {
        T operate() throws Exception;
    }

}
