package hr.prism.board;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.oauth.OAuthGrantRequestAuthenticationResult;
import hr.prism.board.object.AccountPassword;
import hr.prism.board.service.StormpathAccountTestService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class StormpathAccountTest {

    @Inject
    private StormpathAccountTestService stormpathAccountTestService;

    @Test
    @Ignore
    public void shouldCreateAndAuthenticateUser() throws Exception {
        AccountPassword accountPassword = stormpathAccountTestService.createTestAccount();
        Account account = accountPassword.getAccount();

        OAuthGrantRequestAuthenticationResult authenticationResult = stormpathAccountTestService.authenticateTestAccount(account.getEmail(), accountPassword.getPassword());
        Assert.assertNotNull(authenticationResult.getAccessToken());
        Assert.assertNotNull(authenticationResult.getRefreshToken());

        account.delete();
    }

}
