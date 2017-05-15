package hr.prism.board.enums;

import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.authentication.adapter.OauthAdapter;

public enum OauthProvider {
    
    FACEBOOK(FacebookAdapter.class),
    LINKEDIN(LinkedinAdapter.class),
    TWITTER;
    
    private Class<? extends OauthAdapter> oauthAdapter;
    
    OauthProvider() {
    }
    
    OauthProvider(Class<? extends OauthAdapter> oauthAdapter) {
        this.oauthAdapter = oauthAdapter;
    }
    
    public Class<? extends OauthAdapter> getOauthAdapter() {
        return oauthAdapter;
    }
    
    
}
