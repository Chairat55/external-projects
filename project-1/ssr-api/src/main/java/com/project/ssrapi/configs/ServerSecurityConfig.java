package com.project.ssrapi.configs;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {

        auth.userDetailsService(userDetailsServiceBean())
                .passwordEncoder(delegatingPasswordEncoder());
    }

    @Bean
    public DelegatingPasswordEncoder delegatingPasswordEncoder() {

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(
                "bcrypt", encoders);

        return passwordEncoder;
    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean(){
        return new JdbcUserDetails();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean()
            throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/images/**").permitAll()
                .antMatchers( "/oauth/revoke-token", "/oauth/authorize", "/oauth/token").permitAll()
                .and()
                .formLogin().disable()
                .csrf().disable();

        http
                .sessionManagement().maximumSessions(1).expiredUrl("/login");
    }
}
