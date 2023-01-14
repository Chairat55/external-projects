package com.project.ssrapi.configs;

import com.example.shinsiri.entities.User;
import com.example.shinsiri.exceptions.BadRequestException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("กรุณา Login ก่อนใช้งาน");
        }

        return Optional.ofNullable(((User) authentication.getPrincipal()).getUsername());
    }
}
