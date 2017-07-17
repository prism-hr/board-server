package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "activity_dismissal", uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "user_id"}))
public class ActivityDismissal extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Activity getActivity() {
        return activity;
    }

    public ActivityDismissal setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ActivityDismissal setUser(User user) {
        this.user = user;
        return this;
    }

}
