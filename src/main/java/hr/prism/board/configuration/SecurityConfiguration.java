package hr.prism.board.configuration;

import hr.prism.board.authentication.AuthenticationFilter;
import hr.prism.board.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Inject
    private AuthenticationService authenticationService;

    @Value("${session.refreshBeforeExpiration.seconds}")
    private Long sessionRefreshBeforeExpirationSeconds;

    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/api/auth/*", "/api/redirect");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(authenticationService, sessionRefreshBeforeExpirationSeconds);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().csrf().disable().anonymous().disable()
            .authorizeRequests().antMatchers("/api/auth/*", "/api/redirect").permitAll()
            .and().addFilterAt(filter, BasicAuthenticationFilter.class);
    }

}
