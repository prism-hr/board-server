package hr.prism.board.repository;

import hr.prism.board.domain.BoardEntity;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MyRepositoryImpl<ENTITY extends BoardEntity, ID extends Serializable>
    extends SimpleJpaRepository<ENTITY, ID> implements MyRepository<ENTITY, ID> {
    
    public MyRepositoryImpl(JpaEntityInformation<ENTITY, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
    
    @Override
    public <T extends ENTITY> T save(T entity) {
        LocalDateTime baseline = LocalDateTime.now();
        entity.setCreatedTimestamp(baseline);
        update(entity, baseline);
        return super.save(entity);
    }
    
    @Override
    public <T extends ENTITY> void update(T entity, LocalDateTime baseline) {
        entity.setUpdatedTimestamp(baseline);
    }
    
}
