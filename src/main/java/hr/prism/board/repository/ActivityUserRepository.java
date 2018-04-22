package hr.prism.board.repository;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.ActivityUser;
import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ActivityUserRepository extends JpaRepository<ActivityUser, Long> {

    ActivityUser findByActivityAndUser(Activity activity, User user);

    void deleteByUser(User user);

}
