package hr.prism.board.domain;

import com.google.common.base.Joiner;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.ChangeListRepresentation;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static hr.prism.board.utils.ResourceUtils.makeQuarter;
import static java.util.Comparator.comparingLong;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.persistence.DiscriminatorType.STRING;
import static javax.persistence.InheritanceType.SINGLE_TABLE;

@Entity
@Table(name = "resource")
@Inheritance(strategy = SINGLE_TABLE)
@DiscriminatorColumn(name = "scope", discriminatorType = STRING)
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

    @Column(name = "state_change_timestamp")
    private LocalDateTime stateChangeTimestamp;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "summary")
    private String summary;

    @OneToOne
    @JoinColumn(name = "document_logo_id")
    private Document documentLogo;

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

    @OrderBy("id asc")
    @OneToMany(mappedBy = "resource")
    private Set<ResourceTask> tasks = new HashSet<>();

    @Transient
    private List<ActionRepresentation> actions;

    @Transient
    private ChangeListRepresentation changeList;

    @Transient
    private String comment;

    @Transient
    private boolean notificationSuppressedForUser;

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Resource getParent() {
        return parent;
    }

    public void setParent(Resource parent) {
        this.parent = parent;
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

    public LocalDateTime getStateChangeTimestamp() {
        return stateChangeTimestamp;
    }

    public void setStateChangeTimestamp(LocalDateTime stateChangeTimestamp) {
        this.stateChangeTimestamp = stateChangeTimestamp;
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

    public Document getDocumentLogo() {
        return documentLogo;
    }

    public void setDocumentLogo(Document documentLogo) {
        this.documentLogo = documentLogo;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    @SuppressWarnings("WeakerAccess")
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

    public void setCategories(Set<ResourceCategory> categories) {
        this.categories = categories;
    }

    public List<ResourceCategory> getMemberCategories() {
        return getCategories(MEMBER);
    }

    public List<String> getMemberCategoryStrings() {
        return getMemberCategories()
            .stream()
            .map(ResourceCategory::getName)
            .collect(toList());
    }

    public List<ResourceCategory> getPostCategories() {
        return getCategories(POST);
    }

    public List<String> getPostCategoryStrings() {
        return getPostCategories()
            .stream()
            .map(ResourceCategory::getName)
            .collect(toList());
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

    @SuppressWarnings("unused")
    public Set<ResourceOperation> getOperations() {
        return operations;
    }

    @SuppressWarnings("unused")
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

    public boolean isNotificationSuppressedForUser() {
        return notificationSuppressedForUser;
    }

    public void setNotificationSuppressedForUser(boolean notificationSuppressedForUser) {
        this.notificationSuppressedForUser = notificationSuppressedForUser;
    }

    public List<String> getCategoryStrings(CategoryType type) {
        return getCategories(type)
            .stream()
            .map(ResourceCategory::getName)
            .collect(toList());
    }

    public List<State> getParentStates() {
        Resource resource = this;
        List<State> parentStates = new ArrayList<>();
        while (true) {
            Resource parent = resource.getParent();
            if (parent == null || Objects.equals(resource, parent)) {
                break;
            }

            parentStates.add(parent.getState());
            resource = parent;
        }

        return parentStates
            .stream()
            .filter(Objects::nonNull)
            .collect(toList());
    }

    public void setIndexDataAndQuarter() {
        String parentIndexData =
            ofNullable(getParent())
                .filter(parent -> !this.equals(parent))
                .map(Resource::getIndexData)
                .orElse(null);

        String indexData = makeSoundex(
            newArrayList(name, summary));

        String combinedIndexData =
            Stream.of(parentIndexData, indexData)
                .filter(Objects::nonNull)
                .collect(joining(" "));

        if (StringUtils.isNotEmpty(combinedIndexData)) {
            setIndexData(combinedIndexData);
        }

        this.quarter = makeQuarter(getCreatedTimestamp());
    }

    @Override
    public String toString() {
        if (scope == null) {
            return null;
        }

        return Joiner.on(" ").skipNulls().join(scope.name().toLowerCase(), getId());
    }

    private List<ResourceCategory> getCategories(CategoryType type) {
        return categories.stream()
            .filter(category -> category.getType() == type)
            .sorted(comparingLong(ResourceCategory::getId))
            .collect(toList());
    }

}
