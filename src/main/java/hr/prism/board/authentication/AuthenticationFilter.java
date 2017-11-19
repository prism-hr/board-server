package hr.prism.board.authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hr.prism.board.service.AuthenticationService;
import hr.prism.board.utils.BoardUtils;

public class AuthenticationFilter extends OncePerRequestFilter {

    private AuthenticationService authenticationService;

    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        AuthenticationToken authenticationToken = BoardUtils.makeAuthenticationToken(authenticationService, authorization);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

}
