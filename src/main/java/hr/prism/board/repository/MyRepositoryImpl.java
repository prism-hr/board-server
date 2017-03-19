package hr.prism.board.repository;

import hr.prism.board.domain.BoardEntity;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MyRepositoryImpl<T extends BoardEntity, ID extends Serializable>
    extends SimpleJpaRepository<T, ID> implements MyRepository<T, ID> {
    
    public MyRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
    
    @Override
    public <S extends T> S save(S entity) {
        if (entity.getCreatedTimestamp() == null) {
            entity.setCreatedTimestamp(LocalDateTime.now());
        }
    
        entity.setUpdatedTimestamp(LocalDateTime.now());
        return super.save(entity);
    }
    
}
