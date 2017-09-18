package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "activity_user", uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "user_id"}))
public class ActivityUser extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Activity getActivity() {
        return activity;
    }

    public ActivityUser setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ActivityUser setUser(User user) {
        this.user = user;
        return this;
    }

}
