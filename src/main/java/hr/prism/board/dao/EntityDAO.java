package hr.prism.board.dao;

import hr.prism.board.domain.BoardEntity;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Repository
public class EntityDAO {

    @Inject
    private SessionFactory sessionFactory;

    public Long save(BoardEntity entity) {
        if(entity.getCreatedTimestamp() == null) {
            entity.setCreatedTimestamp(LocalDateTime.now());
        }
        entity.setUpdatedTimestamp(LocalDateTime.now());
        return (Long) sessionFactory.getCurrentSession().save(entity);
    }

}
