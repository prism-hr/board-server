package hr.prism.board.repository;

import hr.prism.board.domain.User;

public interface UserRepository extends MyRepository<User, Long> {
    
    User findByStormpathId(String stormpathId);
    
    User findByEmail(String email);
}
