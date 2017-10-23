package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.ChangeListRepresentation;

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
    @JoinColumn(name = "parent_id")
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

    @Column(name = "index_data")
    private String indexData;

    @Column(name = "quarter", nullable = false)
    private String quarter;

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

    @OneToMany(mappedBy = "resource")
    private Set<ResourceSearch> searches = new HashSet<>();

    @OneToMany(mappedBy = "resource")
    private Set<ResourceTask> tasks = new HashSet<>();

    @Transient
    private List<ActionRepresentation> actions;

    @Transient
    private ChangeListRepresentation changeList;

    @Transient
    private String comment;

    public Scope getScope() {
        return scope;
    }

    public Resource getParent() {
        return parent;
    }

    public void setParent(Resource parent) {
        this.parent = parent;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getPreviousState() {
        return previousState;
    }

    public void setPreviousState(State previousState) {
        this.previousState = previousState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Document getDocumentLogo() {
        return documentLogo;
    }

    public void setDocumentLogo(Document documentLogo) {
        this.documentLogo = documentLogo;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getIndexData() {
        return indexData;
    }

    public void setIndexData(String indexData) {
        this.indexData = indexData;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
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

    public Set<ResourceSearch> getSearches() {
        return searches;
    }

    public Set<ResourceTask> getTasks() {
        return tasks;
    }

    public List<ActionRepresentation> getActions() {
        return actions;
    }

    public void setActions(List<ActionRepresentation> actions) {
        this.actions = actions;
    }

    public ChangeListRepresentation getChangeList() {
        return changeList;
    }

    public void setChangeList(ChangeListRepresentation changeList) {
        this.changeList = changeList;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<ResourceCategory> getCategories(CategoryType type) {
        return categories.stream()
            .filter(category -> category.getType() == type && category.getOrdinal() != null)
            .sorted(Comparator.comparingInt(ResourceCategory::getOrdinal))
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        if (scope == null) {
            return null;
        }

        return Joiner.on(" ").skipNulls().join(scope.name().toLowerCase(), getId());
    }

}
