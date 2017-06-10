package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.ResourceChangeListRepresentation;

import javax.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Column(name = "summary")
    private String summary;

    @OneToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

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

    @OneToMany(mappedBy = "resource")
    private Set<ResourceOperation> operations = new HashSet<>();

    @Transient
    private List<ActionRepresentation> actions;

    @Transient
    private ResourceChangeListRepresentation changeList;

    @Transient
    private String comment;

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

    public String getSummary() {
        return summary;
    }

    public Resource setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public Document getDocumentLogo() {
        return documentLogo;
    }

    public Resource setDocumentLogo(Document documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public Resource setLocation(Location location) {
        this.location = location;
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

    public List<ResourceCategory> getMemberCategories() {
        return getCategories(CategoryType.MEMBER);
    }

    public List<ResourceCategory> getPostCategories() {
        return getCategories(CategoryType.POST);
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

    public Set<ResourceOperation> getOperations() {
        return operations;
    }

    public List<ActionRepresentation> getActions() {
        return actions;
    }

    public Resource setActions(List<ActionRepresentation> actions) {
        this.actions = actions;
        return this;
    }

    public ResourceChangeListRepresentation getChangeList() {
        return changeList;
    }

    public Resource setChangeList(ResourceChangeListRepresentation changeList) {
        this.changeList = changeList;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Resource setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public String toString() {
        if (scope == null) {
            return null;
        }

        return Joiner.on(" ").skipNulls().join(scope.name().toLowerCase(), getId());
    }

    public List<ResourceCategory> getCategories(CategoryType type) {
        return categories.stream()
            .filter(category -> category.getType() == type && category.getOrdinal() != null)
            .sorted(Comparator.comparingInt(ResourceCategory::getOrdinal))
            .collect(Collectors.toList());
    }

    public Department getDepartment() {
        Resource r = this;
        while(r != null && r.getScope() != Scope.DEPARTMENT) {
            r = r.getParent();
        }
        return (Department) r;
    }

}
