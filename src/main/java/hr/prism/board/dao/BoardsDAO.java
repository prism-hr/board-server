package hr.prism.board.dao;

import hr.prism.board.domain.Board;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

@Repository
public class BoardsDAO {

    @Inject
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public List<Board> getBoards() {
        return (List<Board>) sessionFactory.getCurrentSession().createCriteria(Board.class)
                .list();
    }

}
