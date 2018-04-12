package hr.prism.board.domain;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "activity_event",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "user_id", "event"}))
public class ActivityEvent extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(STRING)
    @Column(name = "event", nullable = false)
    private hr.prism.board.enums.ActivityEvent event;

    public Activity getActivity() {
        return activity;
    }

    public ActivityEvent setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ActivityEvent setUser(User user) {
        this.user = user;
        return this;
    }

    public hr.prism.board.enums.ActivityEvent getEvent() {
        return event;
    }

    public ActivityEvent setEvent(hr.prism.board.enums.ActivityEvent event) {
        this.event = event;
        return this;
    }

}
