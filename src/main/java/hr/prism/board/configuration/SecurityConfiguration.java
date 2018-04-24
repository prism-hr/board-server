package hr.prism.board.configuration;

import hr.prism.board.authentication.AuthenticationFilter;
import hr.prism.board.service.AuthenticationService;
import hr.prism.board.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Long sessionRefreshBeforeExpirationSeconds;

    private final AuthenticationService authenticationService;

    private final UserService userService;

    @Inject
    public SecurityConfiguration(
        @Value("${session.refreshBeforeExpiration.seconds}") Long sessionRefreshBeforeExpirationSeconds,
        AuthenticationService authenticationService, UserService userService) {
        this.sessionRefreshBeforeExpirationSeconds = sessionRefreshBeforeExpirationSeconds;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/api/auth/*", "/api/redirect");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthenticationFilter filter =
            new AuthenticationFilter(sessionRefreshBeforeExpirationSeconds, authenticationService, userService);
        http.sessionManagement().sessionCreationPolicy(STATELESS)
            .and().csrf().disable().anonymous().disable()
            .authorizeRequests().antMatchers("/api/auth/*", "/api/redirect").permitAll()
            .and().addFilterAt(filter, BasicAuthenticationFilter.class);
    }

}
