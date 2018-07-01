package hr.prism.board.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.prism.board.authentication.AuthenticationToken;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.core.Authentication;

import javax.persistence.*;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static javax.persistence.GenerationType.IDENTITY;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@MappedSuperclass
@JsonIgnoreProperties({"id", "creatorId", "createdTimestamp", "updatedTimestamp"})
public abstract class BoardEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    @SuppressWarnings("unused")
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    @PrePersist
    public void onCreate() {
        if (this.creatorId == null) {
            this.creatorId = getUserId();
        }

        LocalDateTime baseline = now();
        this.createdTimestamp = baseline;
        this.updatedTimestamp = baseline;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedTimestamp = now();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        BoardEntity that = (BoardEntity) other;
        return new EqualsBuilder()
            .append(id, that.id)
            .isEquals();
    }

    private Long getUserId() {
        Authentication authentication = getContext().getAuthentication();
        if (authentication instanceof AuthenticationToken) {
            User user = ((AuthenticationToken) authentication).getUser();
            return user.getId();
        }

        return null;
    }

}
