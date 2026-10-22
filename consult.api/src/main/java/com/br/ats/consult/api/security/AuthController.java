package com.br.ats.consult.api.security;

import com.br.ats.consult.api.security.dto.MessageResponse;
import com.br.ats.consult.api.security.dto.RegisterRequest;
import com.br.ats.consult.api.security.model.UserPrincipal;
import com.br.ats.consult.api.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final RateLimitService rateLimitService;
    private final CsrfTokenRepository csrfTokenRepository;

    @Value("${app.security.rate-limit.ip-max-attempts:50}")
    private int ipMaxAttempts;

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(HttpServletRequest request, HttpServletResponse response) {
        csrfTokenRepository.loadDeferredToken(request, response).get();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody AuthRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse response) {
        String email = normalizeEmail(request.email());
        String ipKey = clientKey(httpRequest, "login");
        String accountKey = "login:account:" + email;
        rateLimitService.check(ipKey, ipMaxAttempts);
        rateLimitService.check(accountKey);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);
        rateLimitService.reset(accountKey);

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.create(jwtToken).toString());
        return ResponseEntity.ok(new MessageResponse("Login efetuado com sucesso"));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(Authentication authentication,
                                                  HttpServletResponse response) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            authService.logout(principal.getUsername());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.clear().toString());
        return ResponseEntity.ok(new MessageResponse("Logout efetuado com sucesso"));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request,
                                                    HttpServletRequest httpRequest,
                                                    HttpServletResponse response) {
        String email = normalizeEmail(request.email());
        rateLimitService.check(clientKey(httpRequest, "register"), ipMaxAttempts);
        rateLimitService.check("register:account:" + email);

        String jwtToken = authService.register(request);
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.create(jwtToken).toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Registo efetuado com sucesso"));
    }

    private String clientKey(HttpServletRequest request, String action) {
        return action + ":ip:" + request.getRemoteAddr();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
