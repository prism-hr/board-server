package hr.prism.board.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "department")
public class Department extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "department")
    private Set<Board> boards = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "member_categories", nullable = false)
    private String memberCategories;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Board> getBoards() {
        return boards;
    }

    public void setBoards(Set<Board> boards) {
        this.boards = boards;
    }

    public Document getDocumentLogo() {
        return documentLogo;
    }

    public void setDocumentLogo(Document documentLogo) {
        this.documentLogo = documentLogo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberCategories() {
        return memberCategories;
    }

    public void setMemberCategories(String memberCategories) {
        this.memberCategories = memberCategories;
    }
}
