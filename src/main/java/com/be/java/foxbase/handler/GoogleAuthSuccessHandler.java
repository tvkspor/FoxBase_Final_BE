package com.be.java.foxbase.handler;

import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.AuthenticationResponse;
import com.be.java.foxbase.repository.UserRepository;
import com.be.java.foxbase.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleAuthSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Value("${client.domain}")
    private String CLIENT_DOMAIN;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String fName = oauth2User.getAttribute("given_name");
        String lName = oauth2User.getAttribute("family_name");
        String picture = oauth2User.getAttribute("picture");

        User user = User.builder()
                .username(email)
                .email(email)
                .fName(fName)
                .lName(lName)
                .balance(0L)
                .avatar(picture)
                .password("")
                .build();

        userRepository.save(user);

        String jwt = authenticationService.generateToken(email);

        response.sendRedirect(CLIENT_DOMAIN + "/oauth2-success?token=" + jwt);
    }
}
