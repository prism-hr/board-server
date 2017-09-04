package hr.prism.board.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.prism.board.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"includePublicResources", "orderStatement"})
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
        statement = "resource.parent_id = :parentId")
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
        statement = "resource.quarter = quarter")
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

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResourceFilterProperty {

        String parameter();

        String statement();

    }

}
