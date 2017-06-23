package hr.prism.board.enums;

import hr.prism.board.domain.User;

public enum RedirectAction {

    LOGIN, REGISTER;

    public static RedirectAction makeForUser(User user) {
        if (user.getPassword() == null && user.getOauthProvider() == null) {
            return REGISTER;
        }

        return LOGIN;
    }

}
