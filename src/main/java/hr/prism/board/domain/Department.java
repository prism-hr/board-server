package hr.prism.board.domain;

import com.stripe.model.Customer;
import hr.prism.board.value.ResourceSearch;

import javax.persistence.*;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Scope.Value.DEPARTMENT;

@Entity
@DiscriminatorValue(value = DEPARTMENT)
@NamedEntityGraph(
    name = "department.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "university"),
        @NamedAttributeNode(value = "documentLogo"),
        @NamedAttributeNode(value = "categories")},
    subgraphs = {
        @NamedSubgraph(
            name = "university",
            attributeNodes = {
                @NamedAttributeNode(value = "documentLogo")})})
@NamedNativeQueries({
    @NamedNativeQuery(
        name = "departmentSearch",
        query =
            "SELECT resource.id as id, resource.name as name, " +
                "document_logo.cloudinary_id as documentLogoCloudinaryId, " +
                "document_logo.cloudinary_url as documentLogoCloudinaryUrl, " +
                "document_logo.file_name as documentLogoFileName, " +
                "IF(resource.name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
                "MATCH (resource.name) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
                "FROM resource " +
                "LEFT JOIN document AS document_logo " +
                "ON resource.document_logo_id = document_logo.id " +
                "WHERE resource.parent_id = :universityId " +
                "AND resource.scope = 'DEPARTMENT' " +
                "AND resource.state = 'ACCEPTED' " +
                "HAVING similarityHard = 1 OR similaritySoft > 0 " +
                "ORDER BY similarityHard DESC, similaritySoft DESC, resource.name " +
                "LIMIT 10",
        resultSetMapping = "departmentSearch"),
    @NamedNativeQuery(
        name = "programSearch",
        query =
            "SELECT user_role.member_program as program, " +
                "IF(user_role.member_program LIKE :searchTermHard, 1, 0) AS similarityHard, " +
                "MATCH (user_role.member_program) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
                "FROM user_role " +
                "WHERE user_role.resource_id = :departmentId " +
                "GROUP BY user_role.member_program " +
                "HAVING similarityHard = 1 OR similaritySoft > 0 " +
                "ORDER BY similarityHard DESC, similaritySoft DESC, user_role.member_program " +
                "LIMIT 10",
        resultSetMapping = "programSearch")})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = "departmentSearch",
        classes = @ConstructorResult(
            targetClass = ResourceSearch.class,
            columns = {
                @ColumnResult(name = "id", type = Long.class),
                @ColumnResult(name = "name", type = String.class),
                @ColumnResult(name = "documentLogoCloudinaryId", type = String.class),
                @ColumnResult(name = "documentLogoCloudinaryUrl", type = String.class),
                @ColumnResult(name = "documentLogoFileName", type = String.class)})),
    @SqlResultSetMapping(
        name = "programSearch",
        columns = @ColumnResult(
            name = "program",
            type = String.class))})
@SuppressWarnings("SqlResolve")
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

    @SuppressWarnings("unused")
    public void setMemberToBeUploadedCount(Long memberToBeUploadedCount) {
        this.memberToBeUploadedCount = memberToBeUploadedCount;
    }

    public LocalDateTime getLastMemberTimestamp() {
        return lastMemberTimestamp;
    }

    public void setLastMemberTimestamp(LocalDateTime lastMemberTimestamp) {
        this.lastMemberTimestamp = lastMemberTimestamp;
    }

    @SuppressWarnings("unused")
    public LocalDateTime getLastTaskCreationTimestamp() {
        return lastTaskCreationTimestamp;
    }

    public void setLastTaskCreationTimestamp(LocalDateTime lastTaskCreationTimestamp) {
        this.lastTaskCreationTimestamp = lastTaskCreationTimestamp;
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
        if (this.memberToBeUploadedCount != null) {
            if (this.memberToBeUploadedCount == 1L) {
                this.memberToBeUploadedCount = null;
            } else {
                this.memberToBeUploadedCount = this.memberToBeUploadedCount - 1L;
            }
        }
    }

}
