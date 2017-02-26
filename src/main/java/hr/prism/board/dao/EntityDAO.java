package hr.prism.board.dao;

import hr.prism.board.domain.BoardEntity;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
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

    public <T> T getByProperty(Class<T> klass, String propertyName, Object propertyValue) {
        return (T) sessionFactory.getCurrentSession().createCriteria(klass) //
                .add(Restrictions.eq(propertyName, propertyValue)) //
                .uniqueResult();
    }

}
