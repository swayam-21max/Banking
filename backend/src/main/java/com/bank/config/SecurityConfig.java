package com.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines a bean for the password encoder.
     * WARNING: Using NoOpPasswordEncoder is insecure as it stores passwords in plain text.
     * This is for demonstration or development purposes ONLY.
     * @return PasswordEncoder instance that does no encoding.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // This returns a PasswordEncoder that does nothing. Passwords will be stored as plain text.
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Configures the security filter chain for the application.
     * @param http HttpSecurity to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity
            .authorizeHttpRequests(authz -> authz
                // Allow public access to login, register, and static resource pages.
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                // All other requests must be authenticated.
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // Custom login page URL
                .defaultSuccessUrl("/dashboard", true) // Redirect here after successful login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL to trigger logout
                .logoutSuccessUrl("/login?logout") // Redirect here after logout
                .permitAll()
            );

        return http.build();
    }
}

