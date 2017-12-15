package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.connect.Connection;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.LinkedInProfile;
import org.springframework.social.linkedin.connect.LinkedInConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class LinkedinAdapter implements OauthAdapter {

    @Value("${auth.linkedin.appSecret}")
    private String linkedinAppSecret;

    @Override
    public User exchangeForUser(SigninDTO signinDTO) {
        try {
            LinkedInConnectionFactory cf = new LinkedInConnectionFactory(signinDTO.getAuthorizationData().getClientId(), linkedinAppSecret);
            AccessGrant accessGrant = cf.getOAuthOperations().exchangeForAccess(
                signinDTO.getOauthData().getCode(), signinDTO.getAuthorizationData().getRedirectUri(), null);
            Connection<LinkedIn> connection = cf.createConnection(accessGrant);
            LinkedInProfile user = connection.getApi().profileOperations().getUserProfile();

            return new User().setGivenName(user.getFirstName()).setSurname(user.getLastName()).setEmail(user.getEmailAddress())
                .setOauthProvider(OauthProvider.LINKEDIN).setOauthAccountId(user.getId()).setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST);
        } catch (ResourceAccessException e) {
            throw new BoardException(ExceptionCode.OAUTH_ERROR, "Fault calling Linkedin API", e);
        }

    }

}
