package hr.prism.board.service;

import hr.prism.board.enums.RedirectAction;

public class RedirectService {

    public static String makeForHome(String serverUrl, RedirectAction action) {
        return serverUrl + "/redirect?" + makeRedirectAction(action);
    }

    public static String makeForResource(String serverUrl, Long resourceId, RedirectAction action) {
        return serverUrl + "/redirect?resource=" + resourceId + "&" + makeRedirectAction(action);
    }

    private static String makeRedirectAction(RedirectAction action) {
        return "action=" + action.name().toLowerCase();
    }

}
