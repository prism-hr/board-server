package hr.prism.board.value;

import hr.prism.board.domain.User;

public class UserNotification {

    private User user;

    private String invitation;

    public UserNotification(User user) {
        this.user = user;
    }

    public UserNotification(User user, String invitation) {
        this.user = user;
        this.invitation = invitation;
    }

    public User getUser() {
        return user;
    }

    public String getInvitation() {
        return invitation;
    }

}
