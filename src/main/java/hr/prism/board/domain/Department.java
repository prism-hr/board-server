package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "department")
public class Department extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;

    @Column(name = "name", nullable = false)
    private String name;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
