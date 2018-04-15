package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import com.stripe.Stripe;
import com.stripe.model.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static hr.prism.board.exception.ExceptionCode.PAYMENT_INTEGRATION_ERROR;

@Service
public class PaymentService {

    @Inject
    public PaymentService(@Value("${stripe.api.secret}") String stripeApiSecret) {
        Stripe.apiKey = stripeApiSecret;
    }

    public Customer getCustomer(String customerId) {
        return performStripeOperation(() ->
                Customer.retrieve(customerId),
            PAYMENT_INTEGRATION_ERROR,
            "Could not get customer: " + customerId);
    }

    public Customer createCustomer(String source) {
        return performStripeOperation(() ->
                Customer.create(ImmutableMap.of("source", source)),
            PAYMENT_INTEGRATION_ERROR,
            "Could not create customer with source: " + source);
    }

    public Customer createSubscription(String customerId) {
        return performStripeOperation(() -> {
                Subscription.create(
                    ImmutableMap.of(
                        "customer", customerId,
                        "items", ImmutableMap.of(
                            "0", ImmutableMap.of(
                                "plan", "department"))));
                return Customer.retrieve(customerId);
            },
            PAYMENT_INTEGRATION_ERROR,
            "Could not create subscription for customer with ID: " + customerId);
    }

    public Customer appendSource(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.getSources().create(ImmutableMap.of("source", source));
                return Customer.retrieve(customerId);
            },
            PAYMENT_INTEGRATION_ERROR,
            "Could not update customer: " + customerId + " with source: " + source);
    }

    public Customer setSourceAsDefault(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.update(ImmutableMap.of("default_source", source));
                return Customer.retrieve(customerId);
            },
            PAYMENT_INTEGRATION_ERROR,
            "Could not not set default source: " + source + " for customer: " + customerId);
    }

    public Customer deleteSource(String customerId, String source) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                customer.getSources().retrieve(source).delete();
                return Customer.retrieve(customerId);
            },
            PAYMENT_INTEGRATION_ERROR,
            "Could not remove source: " + source + " from customer: " + customerId);
    }

    public Customer cancelSubscription(String customerId) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                CustomerSubscriptionCollection subscriptions = customer.getSubscriptions();
                subscriptions.getData().forEach(subscription -> {
                    String subscriptionId = subscription.getId();
                    performStripeOperation(() ->
                            Subscription.retrieve(subscriptionId).cancel(
                                ImmutableMap.of("at_period_end", true)),
                        PAYMENT_INTEGRATION_ERROR,
                        "Could not cancel subscription: " +
                            subscriptionId + " for customer: " + customerId);
                });

                return Customer.retrieve(customerId);
            },
            PAYMENT_INTEGRATION_ERROR,
            "Could not cancel subscription for customer: " + customerId);
    }

    public Customer reactivateSubscription(String customerId) {
        return performStripeOperation(() -> {
                Customer customer = Customer.retrieve(customerId);
                CustomerSubscriptionCollection subscriptions = customer.getSubscriptions();
                subscriptions.getData().forEach(subscription -> {
                    String subscriptionId = subscription.getId();
                    performStripeOperation(() ->
                            Subscription.retrieve(subscriptionId).update(
                                ImmutableMap.of(
                                    "items", ImmutableMap.of(
                                        "0", ImmutableMap.of(
                                            "id", subscription.getSubscriptionItems().getData().get(0).getId(),
                                            "plan", "department")),
                                    "cancel_at_period_end", false)),
                        PAYMENT_INTEGRATION_ERROR,
                        "Could not reactivate subscription: " +
                            subscriptionId + " for customer: " + customerId);
                });

                return Customer.retrieve(customerId);
            },
            PAYMENT_INTEGRATION_ERROR,
            "Could not reactivate subscription for customer: " + customerId);
    }

    public InvoiceCollection getInvoices(String customerId) {
        return performStripeOperation(() ->
                Invoice.list(ImmutableMap.of("customer", customerId)),
            PAYMENT_INTEGRATION_ERROR,
            "Could not get invoices for customer: " + customerId);
    }

    private <T> T performStripeOperation(StripeOperation<T> operation, ExceptionCode exceptionCode,
                                         String exceptionMessage) {
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
