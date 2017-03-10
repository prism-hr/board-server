package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "board")
public class Board extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "post_categories", nullable = false)
    private String postCategories;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPostCategories() {
        return postCategories;
    }

    public void setPostCategories(String postCategories) {
        this.postCategories = postCategories;
    }
}
