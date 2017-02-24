package hr.prism.board.dao;

import hr.prism.board.domain.Department;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

@Repository
public class DepartmentDAO {

    @Inject
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public List<Department> getDepartments() {
        return (List<Department>) sessionFactory.getCurrentSession().createCriteria(Department.class)
                .list();
    }

}
