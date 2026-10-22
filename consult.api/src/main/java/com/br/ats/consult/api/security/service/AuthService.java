package com.br.ats.consult.api.security.service;

import com.br.ats.consult.api.security.JwtService;
import com.br.ats.consult.api.security.dto.RegisterRequest;
import com.br.ats.consult.api.security.exception.EmailAlreadyRegisteredException;
import com.br.ats.consult.api.security.model.Role;
import com.br.ats.consult.api.security.model.User;
import com.br.ats.consult.api.security.model.UserPrincipal;
import com.br.ats.consult.api.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(newUser);

        return jwtService.generateToken(UserPrincipal.from(newUser));
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(normalizeEmail(email))
                .ifPresent(user -> userRepository.incrementTokenVersion(user.getId()));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
