package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import com.stripe.Stripe;
import com.stripe.model.*;
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
            "Could not get customer: " + customerId);
    }

    Customer createCustomer(String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.create(ImmutableMap.of("source", source));
                String customerId = customer.getId();
                Subscription.create(
                    ImmutableMap.of(
                        "customer", customerId,
                        "items", ImmutableMap.of(
                            "0", ImmutableMap.of(
                                "plan", "department"))));
                return Customer.retrieve(customerId);
            },
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
            "Could not update customer: " + customerId + " with source: " + source);
    }

    Customer setSourceAsDefault(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.update(ImmutableMap.of("default_source", source));
                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not not set default source: " + source + " for customer: " + customerId);
    }

    Customer deleteSource(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.getSources().retrieve(source).delete();
                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not remove source: " + source + " from customer: " + customerId);
    }

    Customer cancelSubscription(String customerId) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                CustomerSubscriptionCollection subscriptions = customer.getSubscriptions();
                subscriptions.getData().forEach(subscription -> {
                    String subscriptionId = subscription.getId();
                    performStripeOperation(() ->
                            Subscription.retrieve(subscriptionId).cancel(ImmutableMap.of("at_period_end", true)),
                        ExceptionCode.PAYMENT_INTEGRATION_ERROR,
                        "Could not cancel subscription: " + subscriptionId + " for customer: " + customerId);
                });

                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not cancel subscription for customer: " + customerId);
    }

    Customer reactivateSubscription(String customerId) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                CustomerSubscriptionCollection subscriptions = customer.getSubscriptions();
                subscriptions.getData().forEach(subscription -> {
                    String subscriptionId = subscription.getId();
                    performStripeOperation(() ->
                            Subscription.retrieve(subscriptionId).update(ImmutableMap.of(
                                "items", ImmutableMap.of(
                                    "0", ImmutableMap.of(
                                        "id", subscription.getSubscriptionItems().getData().get(0).getId(),
                                        "plan", "department")))),
                        ExceptionCode.PAYMENT_INTEGRATION_ERROR,
                        "Could not reactivate subscription: " + subscriptionId + " for customer: " + customerId);
                });

                return Customer.retrieve(customerId);
            },
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not reactivate subscription for customer: " + customerId);
    }

    InvoiceCollection getInvoices(String customerId) {
        return performStripeOperation(() ->
                Invoice.list(ImmutableMap.of("customer", customerId)),
            ExceptionCode.PAYMENT_INTEGRATION_ERROR,
            "Could not get invoices for customer: " + customerId);
    }

    private <T> T performStripeOperation(StripeOperation<T> operation, ExceptionCode exceptionCode, String exceptionMessage) {
        try {
            return operation.operate();
        } catch (Exception e) {
            throw new BoardException(exceptionCode, exceptionMessage, e);
        }
    }

    private interface StripeOperation<T> {
        T operate() throws Exception;
    }

}
