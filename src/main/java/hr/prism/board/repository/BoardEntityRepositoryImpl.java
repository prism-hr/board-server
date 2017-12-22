package hr.prism.board.repository;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.BoardEntity;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.time.LocalDateTime;

public class BoardEntityRepositoryImpl<ENTITY extends BoardEntity, ID extends Serializable>
    extends SimpleJpaRepository<ENTITY, ID> implements BoardEntityRepository<ENTITY, ID> {

    public BoardEntityRepositoryImpl(JpaEntityInformation<ENTITY, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public <T extends ENTITY> T save(T entity) {
        if (entity.getCreatorId() == null) {
            AuthenticationToken authentication = (AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                entity.setCreatorId(authentication.getUserId());
            }
        }

        LocalDateTime baseline = LocalDateTime.now();
        entity.setCreatedTimestamp(baseline);
        entity.setUpdatedTimestamp(baseline);
        return super.save(entity);
    }

    @Override
    public <T extends ENTITY> T update(T entity) {
        entity.setUpdatedTimestamp(LocalDateTime.now());
        return entity;
    }

}
