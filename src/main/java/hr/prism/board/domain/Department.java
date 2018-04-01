package hr.prism.board.domain;

import com.stripe.model.Customer;
import com.stripe.model.InvoiceCollection;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.OrganizationSummaryRepresentation;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@SuppressWarnings({"unused", "UnusedReturnValue"})
@DiscriminatorValue(value = Scope.Value.DEPARTMENT)
@NamedEntityGraph(
    name = "department.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "university"),
        @NamedAttributeNode(value = "documentLogo"),
        @NamedAttributeNode(value = "categories"),
        @NamedAttributeNode(value = "tasks", subgraph = "tasks")},
    subgraphs = {
        @NamedSubgraph(
            name = "university",
            attributeNodes = {
                @NamedAttributeNode(value = "documentLogo")}),
        @NamedSubgraph(
            name = "tasks",
            attributeNodes = {
                @NamedAttributeNode(value = "completions")})})
public class Department extends Resource {

    @Column(name = "notified_count")
    private Integer notifiedCount;

    @Column(name = "customer_id", unique = true)
    private String customerId;

    @Column(name = "member_to_be_uploaded_count")
    private Long memberToBeUploadedCount;

    @Column(name = "last_member_timestamp")
    private LocalDateTime lastMemberTimestamp;

    @Column(name = "last_task_creation_timestamp")
    private LocalDateTime lastTaskCreationTimestamp;

    @Transient
    private Customer customer;

    @Transient
    private Long postCount;

    @Transient
    private Long postCountAllTime;

    @Transient
    private LocalDateTime mostRecentPost;

    @Transient
    private Long memberCount;

    @Transient
    private Long memberCountAllTime;

    @Transient
    private LocalDateTime mostRecentMember;

    @Transient
    private List<ResourceTask> userTasks;

    @Transient
    private List<OrganizationSummaryRepresentation> organizations;

    @Transient
    private InvoiceCollection invoices;

    public Integer getNotifiedCount() {
        return notifiedCount;
    }

    public void setNotifiedCount(Integer notifiedCount) {
        this.notifiedCount = notifiedCount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getMemberToBeUploadedCount() {
        return memberToBeUploadedCount;
    }

    public void setMemberToBeUploadedCount(Long memberToBeUploadedCount) {
        this.memberToBeUploadedCount = memberToBeUploadedCount;
    }

    public LocalDateTime getLastMemberTimestamp() {
        return lastMemberTimestamp;
    }

    public void setLastMemberTimestamp(LocalDateTime lastMemberTimestamp) {
        this.lastMemberTimestamp = lastMemberTimestamp;
    }

    public LocalDateTime getLastTaskCreationTimestamp() {
        return lastTaskCreationTimestamp;
    }

    public void setLastTaskCreationTimestamp(LocalDateTime lastTaskCreationTimestamp) {
        this.lastTaskCreationTimestamp = lastTaskCreationTimestamp;
    }

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public Long getPostCountAllTime() {
        return postCountAllTime;
    }

    public void setPostCountAllTime(Long postCountAllTime) {
        this.postCountAllTime = postCountAllTime;
    }

    public LocalDateTime getMostRecentPost() {
        return mostRecentPost;
    }

    public void setMostRecentPost(LocalDateTime mostRecentPost) {
        this.mostRecentPost = mostRecentPost;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }

    public Long getMemberCountAllTime() {
        return memberCountAllTime;
    }

    public void setMemberCountAllTime(Long memberCountAllTime) {
        this.memberCountAllTime = memberCountAllTime;
    }

    public LocalDateTime getMostRecentMember() {
        return mostRecentMember;
    }

    public void setMostRecentMember(LocalDateTime mostRecentMember) {
        this.mostRecentMember = mostRecentMember;
    }

    public List<ResourceTask> getUserTasks() {
        return userTasks;
    }

    public void setUserTasks(List<ResourceTask> userTasks) {
        this.userTasks = userTasks;
    }

    public List<OrganizationSummaryRepresentation> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationSummaryRepresentation> organizations) {
        this.organizations = organizations;
    }

    public InvoiceCollection getInvoices() {
        return invoices;
    }

    public void setInvoices(InvoiceCollection invoices) {
        this.invoices = invoices;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void increaseMemberTobeUploadedCount(Long memberCountPending) {
        if (this.memberToBeUploadedCount == null) {
            this.memberToBeUploadedCount = memberCountPending;
        } else {
            this.memberToBeUploadedCount = this.memberToBeUploadedCount + memberCountPending;
        }
    }

    public void decrementMemberToBeUploadedCount() {
        // We shouldn't ever have null here but if we do, not a good reason to crash the app - just let the count reset itself
        if (this.memberToBeUploadedCount != null) {
            if (this.memberToBeUploadedCount == 1L) {
                this.memberToBeUploadedCount = null;
            } else {
                this.memberToBeUploadedCount = this.memberToBeUploadedCount - 1L;
            }
        }
    }

}
