package hr.prism.board.service;

import com.stripe.model.Customer;
import org.springframework.stereotype.Service;

@Service
public class TestPaymentService extends PaymentService {

    @Override
    Customer getCustomer(String customerId) {
        return mockCustomer();
    }

    @Override
    Customer createCustomer(String source) {
        return mockCustomer();
    }

    @Override
    Customer createSubscription(String customerId) {
        return mockCustomer();
    }

    @Override
    Customer appendSource(String customerId, String source) {
        return mockCustomer();
    }

    @Override
    Customer setSourceAsDefault(String customerId, String source) {
        return mockCustomer();
    }

    @Override
    Customer cancelSubscription(String customerId) {
        return mockCustomer();
    }

    private Customer mockCustomer() {
        Customer customer = new Customer();
        customer.setId("id");
        return customer;
    }

}
