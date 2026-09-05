package com.example.acquitance.auth;

import com.example.acquitance.dto.AuthResultDto;
import com.example.acquitance.service.ExternalLoginService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExternalAuthenticationProvider implements AuthenticationProvider {

    private final ExternalLoginService externalLoginService;

    public ExternalAuthenticationProvider(ExternalLoginService externalLoginService) {
        this.externalLoginService = externalLoginService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        AuthResultDto result = externalLoginService.authenticate(username, password, "siocon", "1");
        if (result != null) {
            // Use the full name extracted from the home page as the principal name
            String displayName = result.getFullName() != null ? result.getFullName() : username;
            
            return new UsernamePasswordAuthenticationToken(
                    displayName, password, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        } else {
            throw new BadCredentialsException("External authentication failed via jrmsu-arms.online");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
