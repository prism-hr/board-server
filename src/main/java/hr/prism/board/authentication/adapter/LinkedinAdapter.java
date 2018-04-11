
package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.exception.BoardException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.connect.Connection;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.LinkedInProfile;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import javax.inject.Inject;

import static hr.prism.board.enums.DocumentRequestState.DISPLAY_FIRST;
import static hr.prism.board.enums.OauthProvider.LINKEDIN;
import static hr.prism.board.exception.ExceptionCode.OAUTH_ERROR;

@Component
public class LinkedinAdapter implements OauthAdapter {

    private final String linkedinAppSecret;

    @Inject
    public LinkedinAdapter(@Value("${auth.linkedin.appSecret}") String linkedinAppSecret) {
        this.linkedinAppSecret = linkedinAppSecret;
    }

    @Override
    public User exchangeForUser(SigninDTO signinDTO) {
        try {
            LinkedInConnectionFactory cf = new LinkedInConnectionFactory(
                signinDTO.getAuthorizationData().getClientId(), linkedinAppSecret);
            AccessGrant accessGrant = cf.getOAuthOperations().exchangeForAccess(
                signinDTO.getOauthData().getCode(),
                signinDTO.getAuthorizationData().getRedirectUri(), null);
            Connection<LinkedIn> connection = cf.createConnection(accessGrant);
            LinkedInProfile user = connection.getApi().profileOperations().getUserProfile();

            return new User()
                .setGivenName(user.getFirstName())
                .setSurname(user.getLastName())
                .setEmail(user.getEmailAddress())
                .setOauthProvider(LINKEDIN)
                .setOauthAccountId(user.getId())
                .setDocumentImageRequestState(DISPLAY_FIRST);
        } catch (ResourceAccessException e) {
            throw new BoardException(OAUTH_ERROR, "Fault calling Linkedin API", e);
        }

    }

}
