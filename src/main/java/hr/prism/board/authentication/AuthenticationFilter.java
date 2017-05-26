package hr.prism.board.authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hr.prism.board.service.UserService;

public class AuthenticationFilter extends OncePerRequestFilter {

    private String jwsSecret;

    public AuthenticationFilter(String jwsSecret) {
        this.jwsSecret = jwsSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String accessToken = authorization.replaceFirst("Bearer ", "");
            Long userId = UserService.decodeAccessToken(accessToken, jwsSecret);

            SecurityContextHolder.getContext().setAuthentication(new AuthenticationToken(userId));
            response.setHeader("Authorization", "Bearer " + UserService.makeAccessToken(userId, jwsSecret));
        }

        filterChain.doFilter(request, response);
    }

}
