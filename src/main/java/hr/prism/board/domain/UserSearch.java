package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "user_search", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "search"}))
public class UserSearch extends Search {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
