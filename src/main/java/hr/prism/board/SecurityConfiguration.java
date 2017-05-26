package hr.prism.board;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;

import hr.prism.board.authentication.AuthenticationFilter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Inject
    private Environment environment;

    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/api/auth/*", "/api/redirect");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().csrf().disable().anonymous().disable()
            .authorizeRequests().antMatchers("/api/auth/*", "/api/redirect").permitAll()
            .and().addFilterAt(new AuthenticationFilter(environment.getProperty("jws.secret")), BasicAuthenticationFilter.class);
    }

}
