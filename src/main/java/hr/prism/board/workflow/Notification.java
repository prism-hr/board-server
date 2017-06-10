package hr.prism.board.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;

public class Notification {

    @JsonIgnore
    private Workflow workflow;

    @JsonIgnore
    private ObjectMapper objectMapper;

    private Scope scope;

    private Role role;

    private boolean excludingCreator = false;

    private String template;

    public Notification setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        return this;
    }

    public Notification setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public Notification setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public Notification setRole(Role role) {
        this.role = role;
        return this;
    }

    public boolean isExcludingCreator() {
        return excludingCreator;
    }

    public Notification excludingCreator() {
        this.excludingCreator = true;
        return this;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Workflow with(String notification) {
        this.template = notification;
        return this.workflow;
    }

    @Override
    public String toString() {
        try {
            return this.objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ApiException(ExceptionCode.PROBLEM, e);
        }
    }

}
