package hr.prism.board.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true, value = {"id", "creator_id", "createdTimestamp", "updatedTimestamp"})
public abstract class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public BoardEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public BoardEntity setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public BoardEntity setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public BoardEntity setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object object) {
        return !(object == null || getClass() != object.getClass()) && Objects.equals(id, ((BoardEntity) object).getId());
    }

}
