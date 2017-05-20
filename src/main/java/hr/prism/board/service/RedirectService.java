package hr.prism.board.service;

public class RedirectService {
    
    public static String makeRedirectForLogin(String serverUrl) {
        return serverUrl + "/redirect?path=login";
    }
    
}
