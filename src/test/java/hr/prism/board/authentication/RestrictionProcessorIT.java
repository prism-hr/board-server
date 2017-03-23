package hr.prism.board.authentication;

import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.api.AbstractIT;
import hr.prism.board.exception.ApiForbiddenException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class RestrictionProcessorIT extends AbstractIT {
    
    @Inject
    private RestrictionProcessor restrictionProcessor;
    
    @Test
    public void shouldForbidUnauthenticatedUser() {
        mockSecurityContext(null);
        
        try {
            restrictionProcessor.processRestriction(null, null);
            Assert.fail();
        } catch (ApiForbiddenException e) {
            Assert.assertEquals("User not authenticated", e.getMessage());
        }
    }
    
    private void mockSecurityContext(org.springframework.security.core.userdetails.User user) {
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
        SecurityContextHolder.setContext(securityContext);
    }
    
}
