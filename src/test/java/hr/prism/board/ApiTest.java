package hr.prism.board;

import hr.prism.board.object.AccountPassword;
import hr.prism.board.service.AccountTestService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.oauth.OAuthGrantRequestAuthenticationResult;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ApiTest {

    @Inject
    private Environment environment;
    
    @Inject
    private AccountTestService accountTestService;

    @Inject
    private Flyway flyway;
    
    @Test
    public void shouldUseTestSchema() {
        Assert.assertEquals("board_test", environment.getProperty("database.schema"));
    }
    
    @Test
    public void shouldCreateAndAuthenticateUser() throws Exception {
        AccountPassword accountPassword = accountTestService.createTestAccount();
        Account account = accountPassword.getAccount();

        OAuthGrantRequestAuthenticationResult authenticationResult = accountTestService.authenticateTestAccount(account.getEmail(), accountPassword.getPassword());
        Assert.assertNotNull(authenticationResult.getAccessToken());
        Assert.assertNotNull(authenticationResult.getRefreshToken());

        account.delete();
    }
    
    @PreDestroy
    private void preDestroy() {
        flyway.clean();
    }

}
