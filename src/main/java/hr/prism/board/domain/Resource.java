package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.State;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "scope", discriminatorType = DiscriminatorType.STRING)
public class Resource extends BoardEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, insertable = false, updatable = false)
    private Scope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;

    @Column(name = "handle")
    private String handle;

    @OneToMany(mappedBy = "parentResource")
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "resource")
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "resource1")
    private Set<ResourceRelation> children = new HashSet<>();

    @OneToMany(mappedBy = "resource2")
    private Set<ResourceRelation> parents = new HashSet<>();

    public Scope getScope() {
        return scope;
    }

    public Resource setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public State getState() {
        return state;
    }

    public Resource setState(State state) {
        this.state = state;
        return this;
    }

    public String getName() {
        return name;
    }

    public Resource setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Resource setDescription(String description) {
        this.description = description;
        return this;
    }

    public Document getDocumentLogo() {
        return documentLogo;
    }

    public Resource setDocumentLogo(Document documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public Resource setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public Set<ResourceRelation> getChildren() {
        return children;
    }

    public Set<ResourceRelation> getParents() {
        return parents;
    }

    @Override
    public String toString() {
        if (scope == null) {
            return null;
        }

        return Joiner.on(" ").skipNulls().join(scope.name().toLowerCase(), getId());
    }

}
