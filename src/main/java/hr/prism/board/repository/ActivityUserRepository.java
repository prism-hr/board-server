package hr.prism.board.repository;

import hr.prism.board.domain.Activity;
import hr.prism.board.domain.ActivityUser;
import hr.prism.board.domain.User;

public interface ActivityUserRepository extends MyRepository<ActivityUser, Long> {

    ActivityUser findByActivityAndUser(Activity activity, User user);

    void deleteByUser(User user);

}
