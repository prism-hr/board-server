package hr.prism.board.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

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
        statement = "resource.state in (:state)")
    private ResourceFilterList<State> state;

    @ResourceFilterProperty(
        parameter = "negatedState",
        statement = "resource.state not in (:negatedState)")
    private ResourceFilterList<State> negatedState;

    @ResourceFilterProperty(
        parameter = "quarter",
        statement = "resource.quarter = :quarter")
    private String quarter;

    private String searchTerm;

    private Action action;

    private String orderStatement;

    private String orderStatementSql;

    public Scope getScope() {
        return scope;
    }

    public ResourceFilter setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Long getId() {
        return id;
    }

    public ResourceFilter setId(Long id) {
        this.id = id;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public ResourceFilter setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    @SuppressWarnings("unused")
    public Long getParentId() {
        return parentId;
    }

    public ResourceFilter setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public ResourceFilterList<State> getState() {
        return state;
    }

    public ResourceFilter setState(ResourceFilterList<State> state) {
        this.state = state;
        return this;
    }

    public ResourceFilterList<State> getNegatedState() {
        return negatedState;
    }

    public ResourceFilter setNegatedState(ResourceFilterList<State> negatedState) {
        this.negatedState = negatedState;
        return this;
    }

    public String getQuarter() {
        return quarter;
    }

    @SuppressWarnings("unused")
    public ResourceFilter setQuarter(String quarter) {
        this.quarter = quarter;
        return this;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public ResourceFilter setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public ResourceFilter setAction(Action action) {
        this.action = action;
        return this;
    }

    public String getOrderStatement() {
        return orderStatement;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceFilter setOrderStatement(String orderStatement) {
        this.orderStatement = orderStatement;
        return this;
    }

    public String getOrderStatementSql() {
        return orderStatementSql;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceFilter setOrderStatementSql(String orderStatementSql) {
        this.orderStatementSql = orderStatementSql;
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
            .append(quarter)
            .append(searchTerm)
            .append(action)
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
            .append(quarter, that.quarter)
            .append(searchTerm, that.searchTerm)
            .append(action, that.action)
            .append(orderStatement, that.orderStatement)
            .isEquals();
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResourceFilterProperty {

        String parameter();

        String statement();

    }

    public static class ResourceFilterList<T> extends ArrayList<T> {

        @Override
        public String toString() {
            return stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        }

        @SafeVarargs
        public static <T> ResourceFilterList<T> of(T... items) {
            ResourceFilterList<T> resourceFilterList = new ResourceFilterList<>();
            if (isNotEmpty(items)) {
                resourceFilterList.addAll(Arrays.asList(items));
            }

            return resourceFilterList;
        }

    }

}
