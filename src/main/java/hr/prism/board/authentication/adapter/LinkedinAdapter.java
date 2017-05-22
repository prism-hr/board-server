package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import org.springframework.core.env.Environment;
import org.springframework.social.linkedin.api.LinkedInProfile;
import org.springframework.social.linkedin.api.impl.LinkedInTemplate;
import org.springframework.social.linkedin.connect.LinkedInServiceProvider;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class LinkedinAdapter implements OauthAdapter {
    
    @Inject
    private Environment environment;
    
    @Override
    public User exchangeForUser(OauthDTO oauthDTO) {
        LinkedInServiceProvider provider = new LinkedInServiceProvider(oauthDTO.getClientId(), environment.getProperty("auth.linkedin.appSecret"));
        AccessGrant accessGrant = provider.getOAuthOperations().exchangeForAccess(oauthDTO.getCode(), oauthDTO.getRedirectUri(), null);
        
        LinkedInTemplate template = new LinkedInTemplate(accessGrant.getAccessToken());
        LinkedInProfile user = template.profileOperations().getUserProfile();
        
        return new User().setGivenName(user.getFirstName()).setSurname(user.getLastName()).setEmail(user.getEmailAddress())
            .setOauthProvider(OauthProvider.LINKEDIN).setOauthAccountId(user.getId()).setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST);
    }
    
}
