package hr.prism.board.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Notification.*;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.*;

@Service
@Transactional
public class Installer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Installer.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    @Inject
    private ObjectMapper objectMapper;

    @PostConstruct
    public void install() {
        Workflow workflow = new Workflow(objectMapper)
            // Department accepted state
            .permitThatAnybody().can(VIEW, DEPARTMENT).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(ACCEPTED).creating(BOARD).inState(DRAFT)
            .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with(NEW_BOARD_PARENT)
            .notifying(BOARD, ADMINISTRATOR).whenTransitioningTo(DRAFT).with(NEW_BOARD)
            .notifying(BOARD, ADMINISTRATOR).whenTransitioningTo(ACCEPTED).with(ACCEPT_BOARD)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, DEPARTMENT).inState(ACCEPTED)

            // Board draft state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, BOARD).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, BOARD).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, BOARD).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, BOARD).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, BOARD).inState(DRAFT).transitioningTo(ACCEPTED)
            .notifying(BOARD, ADMINISTRATOR).with(ACCEPT_BOARD)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, BOARD).inState(DRAFT).transitioningTo(REJECTED)
            .notifying(BOARD, ADMINISTRATOR).with(REJECT_BOARD)

            // Board accepted state
            .permitThatAnybody().can(VIEW, BOARD).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(DRAFT)
            .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with(NEW_POST_PARENT)
            .notifying(BOARD, ADMINISTRATOR).excludingCreator().with(NEW_POST_PARENT)
            .notifying(POST, ADMINISTRATOR).with(NEW_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, BOARD).inState(ACCEPTED).transitioningTo(REJECTED)
            .notifying(BOARD, ADMINISTRATOR).with(REJECT_BOARD)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, AUTHOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with(NEW_POST_PARENT)
            .notifying(BOARD, ADMINISTRATOR).excludingCreator().with(NEW_POST_PARENT)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)

            // Board rejected state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, BOARD).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, BOARD).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, BOARD).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, BOARD).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, BOARD).inState(REJECTED).transitioningTo(PREVIOUS)
            .notifying(BOARD, ADMINISTRATOR).with(RESTORE_BOARD)

            // Post draft state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(DRAFT)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(DRAFT)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(DRAFT)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(DRAFT)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, POST).inState(DRAFT).transitioningTo(ACCEPTED)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(DRAFT).transitioningTo(ACCEPTED)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(DRAFT).transitioningTo(WITHDRAWN)

            // Post pending state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(PENDING)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(PENDING)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(PENDING)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(PENDING)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(PENDING)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(PENDING)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(PENDING)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(PENDING)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(PENDING)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(PENDING).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(PENDING).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(PENDING).transitioningTo(WITHDRAWN)

            // Post accepted state
            .permitThatAnybody().can(VIEW, POST).inState(ACCEPTED).andParentState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(ACCEPTED)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(ACCEPTED).transitioningTo(WITHDRAWN)

            // Post expired state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(EXPIRED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(EXPIRED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(EXPIRED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(EXPIRED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(EXPIRED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(EXPIRED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(EXPIRED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(EXPIRED)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(EXPIRED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(EXPIRED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(EXPIRED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(EXPIRED).transitioningTo(WITHDRAWN)

            // Post suspended state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(SUSPENDED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(SUSPENDED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(SUSPENDED)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, POST).inState(SUSPENDED).transitioningTo(ACCEPTED)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(SUSPENDED).transitioningTo(ACCEPTED)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
            .notifying(POST, ADMINISTRATOR).with(REJECT_POST)
            .permitThat(POST, ADMINISTRATOR).can(CORRECT, POST).inState(SUSPENDED).transitioningTo(DRAFT)
            .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with(CORRECT_POST)
            .notifying(BOARD, ADMINISTRATOR).excludingCreator().with(CORRECT_POST)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(SUSPENDED).transitioningTo(WITHDRAWN)

            // Post rejected state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(REJECTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(ACCEPT, POST).inState(REJECTED).transitioningTo(ACCEPTED)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(REJECTED).transitioningTo(ACCEPTED)
            .notifying(POST, ADMINISTRATOR).with(ACCEPT_POST)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
            .notifying(POST, ADMINISTRATOR).with(SUSPEND_POST)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(REJECTED).transitioningTo(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
            .notifying(POST, ADMINISTRATOR).with(RESTORE_POST)
            .permitThat(BOARD, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
            .notifying(POST, ADMINISTRATOR).with(RESTORE_POST)

            // Post withdrawn state
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(VIEW, POST).inState(WITHDRAWN)
            .permitThat(BOARD, ADMINISTRATOR).can(VIEW, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(VIEW, POST).inState(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(WITHDRAWN)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(WITHDRAWN)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(WITHDRAWN)
            .permitThat(POST, ADMINISTRATOR).can(RESTORE, POST).inState(WITHDRAWN).transitioningTo(PREVIOUS);

        new TransactionTemplate(platformTransactionManager).execute(transactionStatus -> {
            LOGGER.info("Deleting old workflow definition");
            entityManager.createNativeQuery("TRUNCATE TABLE workflow").executeUpdate();

            LOGGER.info("Inserting new workflow definition");
            entityManager.createNativeQuery("INSERT INTO workflow(" +
                "resource1_scope, role, resource2_scope, resource2_state, action, resource3_scope, resource3_state, resource4_state, notification) " +
                "VALUES" + workflow.toString()).executeUpdate();
            return null;
        });
    }

}
