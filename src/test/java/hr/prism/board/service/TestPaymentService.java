package hr.prism.board.service;

import com.stripe.model.Customer;
import org.springframework.stereotype.Service;

@Service
public class TestPaymentService extends PaymentService {


    @Override
    Customer createCustomer(String source) {
        Customer customer = new Customer();
        customer.setId("id");
        return customer;
    }

    @Override
    Customer appendSource(String customerId, String source) {
        Customer customer = new Customer();
        customer.setId("id");
        return customer;
    }

    @Override
    Customer setSourceAsDefault(String customerId, String source) {
        Customer customer = new Customer();
        customer.setId("id");
        return customer;
    }

    @Override
    Customer cancelSubscription(String customerId) {
        Customer customer = new Customer();
        customer.setId("id");
        return customer;
    }

}
