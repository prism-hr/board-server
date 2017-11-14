package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

public class DepartmentSubscriptionDTO {

    @NotEmpty
    private String customerId;

    @NotEmpty
    private String subscriptionId;

    public String getCustomerId() {
        return customerId;
    }

    public DepartmentSubscriptionDTO setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public DepartmentSubscriptionDTO setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

}
