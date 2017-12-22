package hr.prism.board.domain;

import com.stripe.model.Customer;
import hr.prism.board.enums.Scope;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "customer_id", unique = true)
    private String customerId;

    @Column(name = "board_count")
    private Long boardCount;

    @Column(name = "member_count")
    private Long memberCount;

    @Column(name = "member_to_be_uploaded_count")
    private Long memberToBeUploadedCount;

    @Column(name = "last_member_timestamp")
    private LocalDateTime lastMemberTimestamp;

    @Column(name = "last_task_creation_timestamp")
    private LocalDateTime lastTaskCreationTimestamp;

    @Transient
    private Customer customer;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getBoardCount() {
        return boardCount;
    }

    public void setBoardCount(Long boardCount) {
        this.boardCount = boardCount;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
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

    public Department setLastTaskCreationTimestamp(LocalDateTime lastTaskCreationTimestamp) {
        this.lastTaskCreationTimestamp = lastTaskCreationTimestamp;
        return this;
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
