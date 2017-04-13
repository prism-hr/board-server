package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.State;
import hr.prism.board.representation.ActionRepresentation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "resource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "scope", discriminatorType = DiscriminatorType.STRING)
public class Resource extends BoardEntity {
    
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private Resource parent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, insertable = false, updatable = false)
    private Scope scope;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "previousState")
    private State previousState;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @OneToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;
    
    @Column(name = "handle")
    private String handle;
    
    @OneToMany(mappedBy = "resource")
    private Set<ResourceCategory> categories = new HashSet<>();
    
    @OneToMany(mappedBy = "resource")
    private Set<UserRole> userRoles = new HashSet<>();
    
    @OneToMany(mappedBy = "resource1")
    private Set<ResourceRelation> children = new HashSet<>();
    
    @OneToMany(mappedBy = "resource2")
    private Set<ResourceRelation> parents = new HashSet<>();
    
    @Transient
    private List<ActionRepresentation> actions;
    
    public Scope getScope() {
        return scope;
    }
    
    public Resource getParent() {
        return parent;
    }
    
    public Resource setParent(Resource parent) {
        this.parent = parent;
        return this;
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
    
    public State getPreviousState() {
        return previousState;
    }
    
    public Resource setPreviousState(State previousState) {
        this.previousState = previousState;
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
    
    public Set<ResourceCategory> getCategories() {
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
    
    public List<ActionRepresentation> getActions() {
        return actions;
    }
    
    public Resource setActions(List<ActionRepresentation> actions) {
        this.actions = actions;
        return this;
    }
    
    @Override
    public String toString() {
        if (scope == null) {
            return null;
        }
    
        return Joiner.on(" ").skipNulls().join(scope.name().toLowerCase(), getId());
    }
    
}
