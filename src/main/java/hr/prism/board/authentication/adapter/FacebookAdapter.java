package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class FacebookAdapter implements OauthAdapter {

    @Value("${auth.facebook.appSecret}")
    private String facebookAppSecret;

    @Override
    public User exchangeForUser(SigninDTO signinDTO) {
        try {
            FacebookConnectionFactory cf = new FacebookConnectionFactory(signinDTO.getClientId(), facebookAppSecret);
            AccessGrant accessGrant = cf.getOAuthOperations().exchangeForAccess(signinDTO.getCode(), signinDTO.getRedirectUri(), null);
            Connection<Facebook> connection = cf.createConnection(accessGrant);
            org.springframework.social.facebook.api.User user = connection.getApi().fetchObject(
                "me", org.springframework.social.facebook.api.User.class, "first_name", "last_name", "email", "id");

            return new User().setGivenName(user.getFirstName()).setSurname(user.getLastName()).setEmail(user.getEmail())
                .setOauthProvider(OauthProvider.FACEBOOK).setOauthAccountId(user.getId()).setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST);
        } catch (ResourceAccessException e) {
            throw new BoardException(ExceptionCode.FAILING_INTEGRATION, "Fault calling Facebook API", e);
        }
    }

}
