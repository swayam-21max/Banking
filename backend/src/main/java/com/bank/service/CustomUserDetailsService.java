package com.bank.service;

import com.bank.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private BankService bankService;

    /**
     * This method is called by Spring Security to authenticate a user.
     * @param email The email address provided by the user in the login form.
     * @return UserDetails object that Spring Security can use.
     * @throws UsernameNotFoundException if the user is not found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Find the user in our database using the BankService
        User appUser = bankService.getUserByEmail(email);

        // 2. If the user is not found, throw an exception
        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // 3. If the user is found, convert our User object into a Spring Security UserDetails object
        // The constructor takes: username (which is email), password, and authorities (roles).
        // Since we don't use roles, we can provide an empty list.
        return new org.springframework.security.core.userdetails.User(
            appUser.getEmail(),
            appUser.getPassword(),
            Collections.emptyList()
        );
    }
}
