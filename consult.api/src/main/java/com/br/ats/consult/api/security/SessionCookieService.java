package com.br.ats.consult.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieService {

    @Value("${server.servlet.session.cookie.name:ats_session}")
    private String cookieName;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${server.servlet.session.cookie.same-site:Strict}")
    private String cookieSameSite;

    @Value("${server.servlet.session.cookie.path:/}")
    private String cookiePath;

    public ResponseCookie clear() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)
                .sameSite(cookieSameSite)
                .maxAge(0)
                .build();
    }
}
