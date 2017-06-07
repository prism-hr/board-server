package hr.prism.board.service;

import hr.prism.board.domain.Resource;

public class RedirectService {

    public static String makeRedirectForLogin(String serverUrl) {
        return serverUrl + "/redirect?path=login";
    }

    public static String makeRedirectForResource(String serverUrl, Resource resource) {
        String path = null;
        switch (resource.getScope()) {
            case DEPARTMENT:
            case BOARD:
                path = resource.getHandle();
                break;
            case POST:
                path = resource.getParent().getHandle() + "/" + resource.getId();
                break;
        }

        return serverUrl + "/redirect?path=" + path;
    }

}
