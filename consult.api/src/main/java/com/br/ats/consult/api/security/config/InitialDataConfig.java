package com.br.ats.consult.api.security.config;

import com.br.ats.consult.api.security.model.Role;
import com.br.ats.consult.api.security.model.User;
import com.br.ats.consult.api.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitialDataConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-admin.email:}")
    private String adminEmail;

    @Value("${app.seed-admin.password:}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                log.info("Seed do administrador desativado (APP_ADMIN_EMAIL/APP_ADMIN_PASSWORD nao definidos).");
                return;
            }

            String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
            if (!userRepository.existsByEmail(normalizedEmail)) {
                User admin = User.builder()
                        .name("Administrador")
                        .email(normalizedEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(admin);
                log.info("Utilizador administrador inicial criado: {}", normalizedEmail);
            }
        };
    }
}
