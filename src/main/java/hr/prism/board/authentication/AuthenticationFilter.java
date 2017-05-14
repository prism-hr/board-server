package hr.prism.board.authentication;

import hr.prism.board.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter extends OncePerRequestFilter {
    
    private UserService userService;
    
    public AuthenticationFilter(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String accessToken = authorization.replaceFirst("Bearer", "");
            if (accessToken != null) {
                Long userId = userService.decodeAccessToken(accessToken);
                SecurityContextHolder.getContext().setAuthentication(new AuthenticationToken(userId));
                response.setHeader("Authorization", "Bearer" + userService.makeAccessToken(userId));
            }
        }
    }
    
}
