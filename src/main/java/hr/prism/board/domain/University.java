package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.UNIVERSITY)
public class University extends Resource {

    @Column(name = "homepage")
    private String homepage;

    @OneToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public Document getDocumentLogo() {
        return documentLogo;
    }

    public void setDocumentLogo(Document documentLogo) {
        this.documentLogo = documentLogo;
    }

}
