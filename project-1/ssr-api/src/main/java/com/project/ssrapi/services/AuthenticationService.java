package com.project.ssrapi.services;

import com.example.shinsiri.entities.User;
import com.example.shinsiri.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthenticationService {
    @Autowired
    private DefaultTokenServices tokenServices;

    public User checkAuthentication(
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new BadRequestException("กรุณา Login ก่อนใช้งาน");
        }
        return (User) authentication.getPrincipal();
    }

    public void logout(
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            String tokenValue = authHeader.replace("Bearer", "").trim();
            tokenServices.revokeToken(tokenValue);
        }
    }
}
