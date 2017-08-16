package hr.prism.board.value;

import hr.prism.board.enums.Role;

import java.time.LocalDateTime;

public class UserRoleSummary extends Summary<Role> {

    public UserRoleSummary(Role key, Long count, LocalDateTime lastTimestamp) {
        super(key, count, lastTimestamp);
    }

}
