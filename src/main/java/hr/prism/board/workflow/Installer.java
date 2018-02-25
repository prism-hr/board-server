package hr.prism.board.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.utils.ObjectMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Activity.*;
import static hr.prism.board.enums.Notification.*;
import static hr.prism.board.enums.Role.*;
import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.*;

@Component
@SuppressWarnings("unused")
public class Installer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Installer.class);

    @Inject
    private ObjectMapper objectMapper;

    private static final Workflow DEPARTMENT_WORKFLOW =
        new Workflow(ObjectMapperProvider.getObjectMapper())
            // Department draft state
            .permitThatAnybody().can(VIEW, DEPARTMENT).inState(DRAFT)
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(DRAFT).creating(BOARD).inState(DRAFT)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(NEW_BOARD_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, DEPARTMENT).inState(DRAFT).creating(BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUBSCRIBE, DEPARTMENT).inState(DRAFT).transitioningTo(ACCEPTED)

            // Department pending state
            .permitThatAnybody().can(VIEW, DEPARTMENT).inState(PENDING)
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(PENDING).creating(BOARD).inState(DRAFT)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(NEW_BOARD_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(PENDING)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, DEPARTMENT).inState(PENDING).creating(BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUBSCRIBE, DEPARTMENT).inState(PENDING).transitioningTo(ACCEPTED)

            // Department accepted state
            .permitThatAnybody().can(VIEW, DEPARTMENT).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(ACCEPTED).creating(BOARD).inState(DRAFT)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(NEW_BOARD_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, DEPARTMENT).inState(ACCEPTED).creating(BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUBSCRIBE, DEPARTMENT).inState(ACCEPTED).transitioningTo(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(UNSUBSCRIBE, DEPARTMENT).inState(ACCEPTED).transitioningTo(REJECTED)

            // Department suspended state
            .permitThatAnybody().can(VIEW, DEPARTMENT).inState(SUSPENDED)
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(SUSPENDED).creating(BOARD).inState(DRAFT)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(NEW_BOARD_PARENT_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(NEW_BOARD_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, DEPARTMENT).inState(SUSPENDED).creating(BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUBSCRIBE, DEPARTMENT).inState(SUSPENDED).transitioningTo(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(UNSUBSCRIBE, DEPARTMENT).inState(SUSPENDED).transitioningTo(REJECTED)

            // Department rejected state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, DEPARTMENT).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUBSCRIBE, DEPARTMENT).inState(REJECTED).transitioningTo(ACCEPTED);

    private static final Workflow BOARD_WORKFLOW =
        new Workflow(ObjectMapperProvider.getObjectMapper())
            // Board draft state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, BOARD).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, BOARD).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, BOARD).inState(DRAFT).transitioningTo(ACCEPTED)
            .prompting(BOARD, ADMINISTRATOR).with(ACCEPT_BOARD_ACTIVITY)
            .notifying(BOARD, ADMINISTRATOR).with(ACCEPT_BOARD_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, BOARD).inState(DRAFT).transitioningTo(REJECTED)
            .prompting(BOARD, ADMINISTRATOR).with(REJECT_BOARD_ACTIVITY)
            .notifying(BOARD, ADMINISTRATOR).with(REJECT_BOARD_NOTIFICATION)

            // Board accepted state
            .permitThatAnybody().can(VIEW, BOARD).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, BOARD).inState(ACCEPTED).andParentStateNot(REJECTED).creating(POST).inState(DRAFT)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(NEW_POST_PARENT_ACTIVITY)
            .prompting(BOARD, ADMINISTRATOR).with(NEW_POST_PARENT_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(NEW_POST_PARENT_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(NEW_POST_PARENT_NOTIFICATION)
            .notifying(POST, ADMINISTRATOR).with(NEW_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, BOARD).inState(ACCEPTED).transitioningTo(REJECTED)
            .prompting(BOARD, ADMINISTRATOR).with(REJECT_BOARD_ACTIVITY)
            .notifying(BOARD, ADMINISTRATOR).with(REJECT_BOARD_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).andParentStateNot(REJECTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).andParentStateNot(REJECTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, AUTHOR).can(EXTEND, BOARD).inState(ACCEPTED).andParentStateNot(REJECTED).creating(POST).inState(ACCEPTED)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(NEW_POST_PARENT_ACTIVITY)
            .prompting(BOARD, ADMINISTRATOR).with(NEW_POST_PARENT_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(NEW_POST_PARENT_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(NEW_POST_PARENT_NOTIFICATION)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)

            // Board rejected state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, BOARD).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, BOARD).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, BOARD).inState(REJECTED).transitioningTo(PREVIOUS)
            .prompting(BOARD, ADMINISTRATOR).with(RESTORE_BOARD_ACTIVITY)
            .notifying(BOARD, ADMINISTRATOR).with(RESTORE_BOARD_NOTIFICATION)

            // Board archived state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, BOARD).inState(ARCHIVED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, BOARD).inState(ARCHIVED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(ARCHIVED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(ARCHIVED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, BOARD).inState(ARCHIVED).transitioningTo(PREVIOUS)
            .permitThat(BOARD, ADMINISTRATOR).can(RESTORE, BOARD).inState(ARCHIVED).transitioningTo(PREVIOUS);

    private static final Workflow POST_WORKFLOW =
        new Workflow(ObjectMapperProvider.getObjectMapper())
            // Post draft state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(DRAFT)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(DRAFT)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, POST).inState(DRAFT).transitioningTo(ACCEPTED)
            .prompting(POST, ADMINISTRATOR).with(ACCEPT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(DRAFT).transitioningTo(ACCEPTED)
            .prompting(POST, ADMINISTRATOR).with(ACCEPT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(DRAFT).transitioningTo(WITHDRAWN)

            // Post pending state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(PENDING)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(PENDING)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(PENDING)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(PENDING)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(PENDING)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(PENDING)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(PENDING).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(PENDING).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(PENDING).transitioningTo(WITHDRAWN)

            // Post accepted state
            .permitThatAnybody().can(VIEW, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(ACCEPTED).transitioningTo(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(PURSUE, POST).inState(ACCEPTED).andParentStateNot(REJECTED)
            .permitThat(DEPARTMENT, MEMBER).can(PURSUE, POST).inState(ACCEPTED).andParentStateNot(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(PURSUE, POST).inState(ACCEPTED).andParentStateNot(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(PURSUE, POST).inState(ACCEPTED).andParentStateNot(REJECTED)

            // Post expired state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(EXPIRED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(EXPIRED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(EXPIRED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(EXPIRED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(EXPIRED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(EXPIRED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(EXPIRED).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(EXPIRED).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(EXPIRED).transitioningTo(WITHDRAWN)

            // Post suspended state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(SUSPENDED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(SUSPENDED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, POST).inState(SUSPENDED).transitioningTo(ACCEPTED)
            .prompting(POST, ADMINISTRATOR).with(ACCEPT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(SUSPENDED).transitioningTo(ACCEPTED)
            .prompting(POST, ADMINISTRATOR).with(ACCEPT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
            .prompting(POST, ADMINISTRATOR).with(REJECT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(CORRECT, POST).inState(SUSPENDED).transitioningTo(DRAFT)
            .prompting(DEPARTMENT, ADMINISTRATOR).with(CORRECT_POST_ACTIVITY)
            .prompting(BOARD, ADMINISTRATOR).with(CORRECT_POST_ACTIVITY)
            .notifying(DEPARTMENT, ADMINISTRATOR).with(CORRECT_POST_NOTIFICATION)
            .notifying(BOARD, ADMINISTRATOR).with(CORRECT_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(SUSPENDED).transitioningTo(WITHDRAWN)

            // Post rejected state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, POST).inState(REJECTED).transitioningTo(ACCEPTED)
            .prompting(POST, ADMINISTRATOR).with(ACCEPT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(REJECTED).transitioningTo(ACCEPTED)
            .prompting(POST, ADMINISTRATOR).with(ACCEPT_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST_NOTIFICATION)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
            .prompting(POST, ADMINISTRATOR).with(SUSPEND_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST_NOTIFICATION)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(REJECTED).transitioningTo(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
            .prompting(POST, ADMINISTRATOR).with(RESTORE_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(RESTORE_POST_NOTIFICATION)
            .permitThat(BOARD, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
            .prompting(POST, ADMINISTRATOR).with(RESTORE_POST_ACTIVITY)
            .notifying(POST, ADMINISTRATOR).with(RESTORE_POST_NOTIFICATION)

            // Post withdrawn state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(WITHDRAWN)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(WITHDRAWN)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(RESTORE, POST).inState(WITHDRAWN).transitioningTo(PREVIOUS)

            // Post archived state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(ARCHIVED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(ARCHIVED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(ARCHIVED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(ARCHIVED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(ARCHIVED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(ARCHIVED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, POST).inState(ARCHIVED).transitioningTo(PREVIOUS)
            .permitThat(BOARD, ADMINISTRATOR).can(RESTORE, POST).inState(ARCHIVED).transitioningTo(PREVIOUS)
            .permitThat(POST, ADMINISTRATOR).can(RESTORE, POST).inState(ARCHIVED).transitioningTo(PREVIOUS);

    @Inject
    private EntityManager entityManager;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    @PostConstruct
    public void install() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LOGGER.info("Deleting old workflow definition");
            entityManager.createNativeQuery("TRUNCATE TABLE workflow").executeUpdate();

            LOGGER.info("Inserting new workflow definition");
            install(DEPARTMENT_WORKFLOW);
            install(BOARD_WORKFLOW);
            install(POST_WORKFLOW);
            return null;
        });
    }

    private void install(Workflow workflow) {
        entityManager.createNativeQuery("INSERT INTO workflow(" +
            "resource1_scope, role, resource2_scope, resource2_state, action, resource3_scope, resource3_state, resource4_state, activity, notification) " +
            "VALUES" + workflow.toString()).executeUpdate();
    }

}
