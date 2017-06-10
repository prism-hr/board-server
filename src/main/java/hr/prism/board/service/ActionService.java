package hr.prism.board.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.interceptor.StateChangeInterceptor;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.workflow.Execution;

@Service
@Transactional
public class ActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

    @Inject
    private ResourceService resourceService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public Resource executeAction(User user, Resource resource, Action action, Execution execution) {
        List<ActionRepresentation> actions = resource.getActions();
        if (actions != null) {
            for (ActionRepresentation actionRepresentation : actions) {
                if (actionRepresentation.getAction() == action) {
                    resource = execution.execute();
                    if (action.isResourceOperation()) {
                        State state = resource.getState();
                        State newState = actionRepresentation.getState();
                        if (newState == null) {
                            newState = state;
                        } else if (newState == State.PREVIOUS) {
                            newState = resource.getPreviousState();
                        }

                        Class<? extends StateChangeInterceptor> interceptorClass = resource.getScope().stateChangeInterceptorClass;
                        if (interceptorClass != null) {
                            newState = BeanUtils.instantiate(interceptorClass).intercept(resource, newState);
                        }

                        if (state != newState) {
                            resourceService.updateState(resource, newState);
                            resource = resourceService.getResource(user, resource.getScope(), resource.getId());
                        }

                        resourceService.createResourceOperation(resource, action, user);
                    }

                    String notification = actionRepresentation.getNotification();
                    if (notification != null) {
                        applicationEventPublisher.publishEvent(
                            new NotificationEvent(this, user.getId(), resource.getId(), actionRepresentation.getNotification()));
                    }

                    return resource;
                }
            }
        }

        if (user == null) {
            LOGGER.info("Public user cannot " + action.name().toLowerCase() + " " + resource.toString());
            throw new ApiForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        LOGGER.info(user.toString() + " cannot " + action.name().toLowerCase() + " " + resource.toString());
        throw new ApiForbiddenException(ExceptionCode.FORBIDDEN_ACTION);
    }

}
