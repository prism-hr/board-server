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

import static hr.prism.board.domain.Role.ADMINISTRATOR;
import static hr.prism.board.domain.Role.AUTHOR;
import static hr.prism.board.domain.Scope.BOARD;
import static hr.prism.board.domain.Scope.DEPARTMENT;
import static hr.prism.board.domain.Scope.POST;
import static hr.prism.board.enums.Action.ACCEPT;
import static hr.prism.board.enums.Action.AUDIT;
import static hr.prism.board.enums.Action.CORRECT;
import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.Action.REJECT;
import static hr.prism.board.enums.Action.RESTORE;
import static hr.prism.board.enums.Action.SUSPEND;
import static hr.prism.board.enums.Action.VIEW;
import static hr.prism.board.enums.Action.WITHDRAW;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.enums.State.EXPIRED;
import static hr.prism.board.enums.State.PENDING;
import static hr.prism.board.enums.State.PREVIOUS;
import static hr.prism.board.enums.State.REJECTED;
import static hr.prism.board.enums.State.SUSPENDED;
import static hr.prism.board.enums.State.WITHDRAWN;

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
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(ACCEPTED).creating(BOARD).inState(ACCEPTED)
                .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with("new_board")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, DEPARTMENT).inState(ACCEPTED)

            // Board accepted state
            .permitThatAnybody().can(VIEW, BOARD).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(DRAFT)
                .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with("new_post_parent")
                .notifying(BOARD, ADMINISTRATOR).excludingCreator().with("new_post_parent")
                .notifying(POST, ADMINISTRATOR).with("new_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, AUTHOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
                .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with("new_post_parent")
                .notifying(BOARD, ADMINISTRATOR).excludingCreator().with("new_post_parent")
                .notifying(POST, ADMINISTRATOR).with("accept_post")

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
                .notifying(POST, ADMINISTRATOR).with("accept_post")
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(DRAFT).transitioningTo(ACCEPTED)
                .notifying(POST, ADMINISTRATOR).with("accept_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
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
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(PENDING).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(PENDING).transitioningTo(WITHDRAWN)

            // Post accepted state
            .permitThatAnybody().can(VIEW, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(POST, ADMINISTRATOR).can(EDIT, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, POST).inState(ACCEPTED)
            .permitThat(POST, ADMINISTRATOR).can(AUDIT, POST).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
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
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(EXPIRED).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
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
                .notifying(POST, ADMINISTRATOR).with("accept_post")
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(SUSPENDED).transitioningTo(ACCEPTED)
                .notifying(POST, ADMINISTRATOR).with("accept_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
                .notifying(POST, ADMINISTRATOR).with("reject_post")
            .permitThat(POST, ADMINISTRATOR).can(CORRECT, POST).inState(SUSPENDED).transitioningTo(DRAFT)
                .notifying(DEPARTMENT, ADMINISTRATOR).excludingCreator().with("correct_post")
                .notifying(BOARD, ADMINISTRATOR).excludingCreator().with("correct_post")
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
                .notifying(POST, ADMINISTRATOR).with("accept_post")
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(REJECTED).transitioningTo(ACCEPTED)
                .notifying(POST, ADMINISTRATOR).with("accept_post")
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
                .notifying(POST, ADMINISTRATOR).with("suspend_post")
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(REJECTED).transitioningTo(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
                .notifying(POST, ADMINISTRATOR).with("restore_post")
            .permitThat(BOARD, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
                .notifying(POST, ADMINISTRATOR).with("restore_post")

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

        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.execute(transactionStatus -> {
            LOGGER.info("Deleting old workflow definition");
            entityManager.createNativeQuery("TRUNCATE TABLE workflow").executeUpdate();

            LOGGER.info("Inserting new workflow definition");
            entityManager.createNativeQuery("INSERT INTO workflow(" +
                "resource1_scope, role, resource2_scope, resource2_state, action, resource3_scope, resource3_state, notification) " +
                "VALUES" + workflow.toString()).executeUpdate();

            return null;
        });
    }

}
