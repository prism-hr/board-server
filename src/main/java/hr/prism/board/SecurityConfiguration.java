package hr.prism.board;

import hr.prism.board.authentication.AuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/auth/*");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .anonymous().disable()
            .authorizeRequests()
            .antMatchers("/auth/*").permitAll()
            .and().addFilterAt(new AuthenticationFilter(), BasicAuthenticationFilter.class);
    }
    
}
