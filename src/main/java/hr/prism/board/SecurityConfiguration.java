package hr.prism.board;

import hr.prism.board.authentication.AuthenticationFilter;
import hr.prism.board.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Inject
    private UserService userService;
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/public/*", "/api/definitions");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .anyRequest().fullyAuthenticated()
            .and().addFilterAt(new AuthenticationFilter(userService), BasicAuthenticationFilter.class);
    }
    
}
