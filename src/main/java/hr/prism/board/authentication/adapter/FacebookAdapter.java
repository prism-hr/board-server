package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.exception.BoardException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.ServerException;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import javax.inject.Inject;

import static hr.prism.board.enums.DocumentRequestState.DISPLAY_FIRST;
import static hr.prism.board.enums.OauthProvider.FACEBOOK;
import static hr.prism.board.exception.ExceptionCode.OAUTH_ERROR;

@Component
public class FacebookAdapter implements OauthAdapter {

    private final String facebookAppSecret;

    @Inject
    public FacebookAdapter(@Value("${auth.facebook.appSecret}") String facebookAppSecret) {
        this.facebookAppSecret = facebookAppSecret;
    }

    @Override
    public User exchangeForUser(SigninDTO signinDTO) {
        try {
            FacebookConnectionFactory cf = new FacebookConnectionFactory(
                signinDTO.getAuthorizationData().getClientId(), facebookAppSecret);
            AccessGrant accessGrant = cf.getOAuthOperations().exchangeForAccess(
                signinDTO.getOauthData().getCode(),
                signinDTO.getAuthorizationData().getRedirectUri(), null);
            Connection<Facebook> connection = cf.createConnection(accessGrant);
            org.springframework.social.facebook.api.User user = connection.getApi().fetchObject("me",
                org.springframework.social.facebook.api.User.class, "first_name", "last_name", "email", "id");

            return new User()
                .setGivenName(user.getFirstName())
                .setSurname(user.getLastName())
                .setEmail(user.getEmail())
                .setOauthProvider(FACEBOOK)
                .setOauthAccountId(user.getId())
                .setDocumentImageRequestState(DISPLAY_FIRST);
        } catch (ResourceAccessException | ServerException e) {
            throw new BoardException(OAUTH_ERROR, "Fault calling Facebook API", e);
        }
    }

}
