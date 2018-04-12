package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "user_notification_suppression",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "resource_id"}))
public class UserNotificationSuppression extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    public User getUser() {
        return user;
    }

    public UserNotificationSuppression setUser(User user) {
        this.user = user;
        return this;
    }

    public Resource getResource() {
        return resource;
    }

    public UserNotificationSuppression setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

}
