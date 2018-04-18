package hr.prism.board.repository;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.BoardEntity;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.time.LocalDateTime;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

public class BoardEntityRepositoryImpl<ENTITY extends BoardEntity, ID extends Serializable>
    extends SimpleJpaRepository<ENTITY, ID> implements BoardEntityRepository<ENTITY, ID> {

    public BoardEntityRepositoryImpl(JpaEntityInformation<ENTITY, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    @Transactional
    public <T extends ENTITY> T save(T entity) {
        if (entity.getCreatorId() == null) {
            AuthenticationToken authentication = (AuthenticationToken) getContext().getAuthentication();
            if (authentication != null) {
                entity.setCreatorId(authentication.getUser());
            }
        }

        LocalDateTime baseline = LocalDateTime.now();
        entity.setCreatedTimestamp(baseline);
        entity.setUpdatedTimestamp(baseline);
        return super.save(entity);
    }

    @Override
    @Transactional
    public <T extends ENTITY> T update(T entity) {
        entity.setUpdatedTimestamp(LocalDateTime.now());
        return super.save(entity);
    }

    @Transactional
    public <T extends ENTITY> T updateSilently(T entity) {
        return super.save(entity);
    }

}
