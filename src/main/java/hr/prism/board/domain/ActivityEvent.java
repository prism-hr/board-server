package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "activity_event", uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "user_id"}))
public class ActivityEvent extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private hr.prism.board.enums.ActivityEvent event;

    @Column(name = "event_count")
    private Long eventCount;

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

    public Long getEventCount() {
        return eventCount;
    }

    public ActivityEvent setEventCount(Long eventCount) {
        this.eventCount = eventCount;
        return this;
    }

}
