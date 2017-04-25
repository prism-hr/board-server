package hr.prism.board.permission;

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
import static hr.prism.board.domain.Scope.*;
import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.State.*;

@Service
@Transactional
public class PermissionInstaller {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionInstaller.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;
    
    @PostConstruct
    public void install() {
        Permissions permissions = new Permissions()
            // Department accepted state
            .permitThatAnybody().can(VIEW, DEPARTMENT).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, DEPARTMENT).inState(ACCEPTED).creating(BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, DEPARTMENT).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, DEPARTMENT).inState(ACCEPTED)
    
            // Board accepted state
            .permitThatAnybody().can(VIEW, BOARD).inState(ACCEPTED)
            .permitThatAnybody().can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(DRAFT)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(AUDIT, BOARD).inState(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, ADMINISTRATOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
            .permitThat(BOARD, AUTHOR).can(EXTEND, BOARD).inState(ACCEPTED).creating(POST).inState(ACCEPTED)
    
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
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(DRAFT).transitioningTo(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(DRAFT).transitioningTo(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(DRAFT).transitioningTo(REJECTED)
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
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(PENDING).transitioningTo(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(PENDING).transitioningTo(REJECTED)
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
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(ACCEPTED).transitioningTo(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(ACCEPTED).transitioningTo(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(ACCEPTED).transitioningTo(WITHDRAWN)
    
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
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(SUSPENDED).transitioningTo(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(SUSPENDED).transitioningTo(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(CORRECT, POST).inState(SUSPENDED).transitioningTo(DRAFT)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(SUSPENDED).transitioningTo(WITHDRAWN)
    
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
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(EXPIRED).transitioningTo(SUSPENDED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
            .permitThat(BOARD, ADMINISTRATOR).can(REJECT, POST).inState(EXPIRED).transitioningTo(REJECTED)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(EXPIRED).transitioningTo(WITHDRAWN)
    
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
            .permitThat(BOARD, ADMINISTRATOR).can(ACCEPT, POST).inState(REJECTED).transitioningTo(ACCEPTED)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
            .permitThat(BOARD, ADMINISTRATOR).can(SUSPEND, POST).inState(REJECTED).transitioningTo(SUSPENDED)
            .permitThat(POST, ADMINISTRATOR).can(WITHDRAW, POST).inState(REJECTED).transitioningTo(WITHDRAWN)
            .permitThat(DEPARTMENT, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
            .permitThat(BOARD, ADMINISTRATOR).can(RESTORE, POST).inState(REJECTED).transitioningTo(PREVIOUS)
    
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
            LOGGER.info("Deleting old permission definitions");
            entityManager.createNativeQuery("TRUNCATE TABLE permission").executeUpdate();
    
            LOGGER.info("Inserting new permission definitions");
            entityManager.createNativeQuery("INSERT INTO permission(resource1_scope, role, resource2_scope, resource2_state, action, resource3_scope, resource3_state) " +
                "VALUES" + permissions.toString()).executeUpdate();
    
            return null;
        });
    }
    
}
