package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.enums.OauthProvider;
import org.springframework.core.env.Environment;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class FacebookAdapter implements OauthAdapter {
    
    @Inject
    private Environment environment;
    
    @Override
    public User exchangeForUser(OauthDTO oauthDTO) {
        FacebookServiceProvider provider = new FacebookServiceProvider(oauthDTO.getClientId(), environment.getProperty("oauth.facebook.appSecret"), null);
        AccessGrant accessGrant = provider.getOAuthOperations().exchangeForAccess(oauthDTO.getCode(), oauthDTO.getRedirectUri(), null);
        
        FacebookTemplate template = new FacebookTemplate(accessGrant.getAccessToken());
        org.springframework.social.facebook.api.User user = template.userOperations().getUserProfile();
        if (user == null) {
            return null;
        }
        
        return new User().setGivenName(user.getFirstName()).setSurname(user.getLastName())
            .setEmail(user.getEmail()).setOauthProvider(OauthProvider.FACEBOOK).setOauthAccountId(user.getId());
    }
    
}
