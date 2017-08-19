package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class FacebookAdapter implements OauthAdapter {

    @Value("${auth.facebook.appSecret]")
    private String facebookAppSecret;

    @Override
    public User exchangeForUser(OauthDTO oauthDTO) {
        FacebookConnectionFactory cf = new FacebookConnectionFactory(oauthDTO.getClientId(), facebookAppSecret);
        try {
            AccessGrant accessGrant = cf.getOAuthOperations().exchangeForAccess(oauthDTO.getCode(), oauthDTO.getRedirectUri(), null);
            Connection<Facebook> connection = cf.createConnection(accessGrant);
            ConnectionData data = connection.createData();
            org.springframework.social.facebook.api.User user = connection.getApi().fetchObject(
                "me", org.springframework.social.facebook.api.User.class, "first_name", "last_name", "email", "id");

            return new User().setGivenName(user.getFirstName()).setSurname(user.getLastName()).setEmail(user.getEmail())
                .setOauthProvider(OauthProvider.FACEBOOK).setOauthAccountId(user.getId()).setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST);
        } catch (ResourceAccessException e) {
            throw new Error(e); // TODO I experienced this exception couple of times, need exception code for that
        }
    }

}
