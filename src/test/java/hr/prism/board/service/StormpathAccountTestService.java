package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.oauth.Authenticators;
import com.stormpath.sdk.oauth.OAuthGrantRequestAuthenticationResult;
import com.stormpath.sdk.oauth.OAuthRequests;
import hr.prism.board.object.AccountPassword;
import org.springframework.stereotype.Service;

@Service
public class StormpathAccountTestService {

    private static int TEST_ACCOUNT_INDEX = 1;

    private Client client;

    private Application application;

    public StormpathAccountTestService(Client client, Application application) {
        this.client = client;
        this.application = application;
    }

    public AccountPassword createTestAccount() {
        Integer index;
        synchronized (StormpathAccountTestService.class) {
            index = TEST_ACCOUNT_INDEX;
            TEST_ACCOUNT_INDEX++;
        }

        Account account = client.instantiate(Account.class);
        account.setGivenName("GivenName" + index);
        account.setSurname("Surname" + index);
        account.setEmail("email" + index + "@prism.board");

        String password = "Password" + index;
        account.setPassword(password);
        return new AccountPassword().setAccount(application.createAccount(account)).setPassword(password);
    }

    public OAuthGrantRequestAuthenticationResult authenticateTestAccount(String email, String password) {
        return Authenticators.OAUTH_PASSWORD_GRANT_REQUEST_AUTHENTICATOR
            .forApplication(application)
            .authenticate(OAuthRequests.OAUTH_PASSWORD_GRANT_REQUEST.builder()
                .setLogin(email)
                .setPassword(password)
                .build());
    }

}
