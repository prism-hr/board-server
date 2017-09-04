package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "user_search", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "search"}))
public class UserSearch extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "search", nullable = false)
    private String search;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
