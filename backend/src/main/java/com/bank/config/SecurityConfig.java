package com.bank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    // --- POJO for JSON login ---
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {

        // --- JSON login filter ---
        UsernamePasswordAuthenticationFilter jsonLoginFilter = new UsernamePasswordAuthenticationFilter() {
            @Override
            public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
                    throws AuthenticationException {
                try {
                    LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
                    return authManager.authenticate(authToken);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void successfulAuthentication(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    FilterChain chain,
                                                    Authentication authResult) {
                response.setStatus(HttpServletResponse.SC_OK);
            }

            @Override
            protected void unsuccessfulAuthentication(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      AuthenticationException failed) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        };
        jsonLoginFilter.setFilterProcessesUrl("/api/login");

        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for frontend POST requests
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000")); // React frontend
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register", "/api/login").permitAll() // Allow public endpoints
                .anyRequest().authenticated() // All other requests require auth
            )
            .addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class); // register custom login

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
