package com.br.ats.consult.api.security.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class SecurityConfigValidator {

    private static final Set<String> VALID_SAME_SITE = Set.of("strict", "lax", "none");

    private final Environment environment;

    @Value("${app.security.csrf.enabled:true}")
    private boolean csrfEnabled;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Strict}")
    private String cookieSameSite;

    @Value("${app.security.cookie.max-age-seconds:28800}")
    private long cookieMaxAgeSeconds;

    public SecurityConfigValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void validate() {
        String sameSite = cookieSameSite == null ? "" : cookieSameSite.trim().toLowerCase(Locale.ROOT);

        if (!VALID_SAME_SITE.contains(sameSite)) {
            throw new IllegalStateException("COOKIE_SAME_SITE deve ser Strict, Lax ou None.");
        }
        if ("none".equals(sameSite) && !cookieSecure) {
            throw new IllegalStateException("COOKIE_SAME_SITE=None exige COOKIE_SECURE=true.");
        }
        if (cookieMaxAgeSeconds <= 0) {
            throw new IllegalStateException("COOKIE_MAX_AGE_SECONDS deve ser maior que zero.");
        }

        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            if (!csrfEnabled) {
                throw new IllegalStateException("CSRF deve estar habilitado no perfil prod.");
            }
            if (!cookieSecure) {
                throw new IllegalStateException("COOKIE_SECURE deve ser true no perfil prod.");
            }
        }
    }
}
