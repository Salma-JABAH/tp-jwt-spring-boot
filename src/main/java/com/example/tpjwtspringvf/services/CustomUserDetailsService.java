package com.example.tpjwtspringvf.services;

import com.example.tpjwtspringvf.entity.AppUser;
import com.example.tpjwtspringvf.repository.UserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository ;


    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) {
        System.out.println("Authenticating user: " + email);
        AppUser appUser =  userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("userNotFound"));
        System.out.println("User loaded: " + appUser.getEmail() + " role=" + appUser.getRole());
        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())
                .roles(appUser.getRole())
                .build();
    }
}
