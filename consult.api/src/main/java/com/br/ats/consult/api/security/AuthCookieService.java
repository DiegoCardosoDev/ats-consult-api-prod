package com.br.ats.consult.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    public static final String COOKIE_NAME = "auth_token";

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Strict}")
    private String cookieSameSite;

    @Value("${app.security.cookie.max-age-seconds:28800}")
    private long cookieMaxAgeSeconds;

    public ResponseCookie create(String jwtToken) {
        return baseCookie(jwtToken)
                .maxAge(Duration.ofSeconds(cookieMaxAgeSeconds))
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite);
    }
}
