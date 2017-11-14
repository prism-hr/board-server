package hr.prism.board.representation;

public class DepartmentSubscriptionRepresentation {

    private String customerId;

    private String subscriptionId;

    public String getCustomerId() {
        return customerId;
    }

    public DepartmentSubscriptionRepresentation setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public DepartmentSubscriptionRepresentation setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

}
