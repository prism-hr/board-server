package hr.prism.board.configuration;

import com.pusher.rest.Pusher;
import hr.prism.board.authentication.AuthenticationFilter;
import hr.prism.board.authentication.AuthorizationHeaderResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.inject.Inject;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${pusher.app}")
    private String pusherApp;

    @Value("${pusher.key}")
    private String pusherKey;

    @Value("${pusher.secret}")
    private String pusherSecret;

    @Value("${pusher.cluster}")
    private String pusherCluster;

    @Inject
    private AuthorizationHeaderResolver authorizationHeaderResolver;

    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/api/auth/*", "/api/redirect");
    }

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(pusherApp, pusherKey, pusherSecret);
        pusher.setCluster(pusherCluster);
        return pusher;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().csrf().disable().anonymous().disable()
            .authorizeRequests().antMatchers("/api/auth/*", "/api/redirect").permitAll()
            .and().addFilterAt(new AuthenticationFilter(authorizationHeaderResolver), BasicAuthenticationFilter.class);
    }

}
