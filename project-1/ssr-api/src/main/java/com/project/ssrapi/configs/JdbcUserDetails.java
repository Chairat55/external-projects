package com.project.ssrapi.configs;

import com.example.shinsiri.entities.User;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.exceptions.UnauthorizedException;
import com.example.shinsiri.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JdbcUserDetails implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String authenticate) {
        User user;

        if (authenticate != null) {
            user = userRepository.findOneByUsername(authenticate);

            if (user != null) {
                return user;
            } else {
                throw new UnauthorizedException("Username ไม่มีในระบบ");
            }
        } else {
            throw new BadRequestException("กรุณากรอก Username");
        }
    }
}