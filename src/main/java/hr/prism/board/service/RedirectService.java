package hr.prism.board.service;

import hr.prism.board.domain.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RedirectService {

    public static String makeRedirectForLogin(String serverUrl) {
        try {
            return serverUrl + "/redirect?path=" + URLEncoder.encode("?showLogin=true", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
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
