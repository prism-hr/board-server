package hr.prism.board.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.prism.board.enums.Scope;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@JsonIgnoreProperties({"includePublicResources", "orderStatement"})
public class ResourceFilter {

    @ResourceFilterProperty(
        parameter = "scope",
        statement = "resource.scope = :scope")
    private Scope scope;

    @ResourceFilterProperty(
        parameter = "id",
        statement = "resource.id = :id")
    private Long id;

    @ResourceFilterProperty(
        parameter = "handle",
        statement = "resource.handle = :handle")
    private String handle;

    @ResourceFilterProperty(
        parameter = "parentId",
        statement = "owner.id = :parentId")
    private Long parentId;

    @ResourceFilterProperty(
        parameter = "state",
        statement = "resource.state = :state")
    private String state;

    @ResourceFilterProperty(
        parameter = "negatedState",
        statement = "resource.state <> :negatedState")
    private String negatedState;

    @ResourceFilterProperty(
        parameter = "quarter",
        statement = "resource.quarter = :quarter")
    private String quarter;

    private String searchTerm;

    private Boolean includePublicResources;

    private String orderStatement;

    public Scope getScope() {
        return scope;
    }

    public hr.prism.board.value.ResourceFilter setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Long getId() {
        return id;
    }

    public hr.prism.board.value.ResourceFilter setId(Long id) {
        this.id = id;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public hr.prism.board.value.ResourceFilter setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    @SuppressWarnings("unused")
    public Long getParentId() {
        return parentId;
    }

    public hr.prism.board.value.ResourceFilter setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getState() {
        return state;
    }

    public ResourceFilter setState(String state) {
        this.state = state;
        return this;
    }

    @SuppressWarnings("unused")
    public String getNegatedState() {
        return negatedState;
    }

    public ResourceFilter setNegatedState(String negatedState) {
        this.negatedState = negatedState;
        return this;
    }

    public String getQuarter() {
        return quarter;
    }

    public hr.prism.board.value.ResourceFilter setQuarter(String quarter) {
        this.quarter = quarter;
        return this;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public hr.prism.board.value.ResourceFilter setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        return this;
    }

    public Boolean getIncludePublicResources() {
        return includePublicResources;
    }

    public hr.prism.board.value.ResourceFilter setIncludePublicResources(Boolean includePublicResources) {
        this.includePublicResources = includePublicResources;
        return this;
    }

    public String getOrderStatement() {
        return orderStatement;
    }

    public hr.prism.board.value.ResourceFilter setOrderStatement(String orderStatement) {
        this.orderStatement = orderStatement;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(scope)
            .append(id)
            .append(handle)
            .append(parentId)
            .append(state)
            .append(negatedState)
            .append(quarter)
            .append(searchTerm)
            .append(includePublicResources)
            .append(orderStatement)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ResourceFilter that = (ResourceFilter) other;
        return new EqualsBuilder()
            .append(scope, that.scope)
            .append(id, that.id)
            .append(handle, that.handle)
            .append(parentId, that.parentId)
            .append(state, that.state)
            .append(negatedState, that.negatedState)
            .append(quarter, that.quarter)
            .append(searchTerm, that.searchTerm)
            .append(includePublicResources, that.includePublicResources)
            .append(orderStatement, that.orderStatement)
            .isEquals();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResourceFilterProperty {

        String parameter();

        String statement();

    }

}
