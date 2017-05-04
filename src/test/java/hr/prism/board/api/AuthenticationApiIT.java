package hr.prism.board.api;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.oauth.OAuthGrantRequestAuthenticationResult;
import hr.prism.board.TestContext;
import hr.prism.board.object.AccountPassword;
import hr.prism.board.service.TestStormpathAccountService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@TestContext
@RunWith(SpringRunner.class)
public class AuthenticationApiIT {
    
    @Inject
    private TestStormpathAccountService testStormpathAccountService;
    
    @Test
    @Ignore
    public void shouldCreateAndAuthenticateUser() throws Exception {
        AccountPassword accountPassword = testStormpathAccountService.createTestAccount();
        Account account = accountPassword.getAccount();
    
        OAuthGrantRequestAuthenticationResult authenticationResult = testStormpathAccountService.authenticateTestAccount(account.getEmail(), accountPassword.getPassword());
        Assert.assertNotNull(authenticationResult.getAccessToken());
        Assert.assertNotNull(authenticationResult.getRefreshToken());
        account.delete();
    }
    
}
