package com.br.ats.consult.api.security;

import com.br.ats.consult.api.security.model.TokenVersionedUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    static final String TOKEN_VERSION_CLAIM = "ver";

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.expiration}")
    private long expirationTime;

    @Value("${api.security.token.issuer:ats-api}")
    private String issuer;

    @Value("${api.security.token.audience:ats-frontend}")
    private String audience;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente JWT_SECRET não está definida. A aplicação não pode iniciar sem uma chave JWT válida.");
        }
        if (expirationTime <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS deve ser maior que zero.");
        }
        if (issuer == null || issuer.isBlank() || audience == null || audience.isBlank()) {
            throw new IllegalStateException("O issuer e a audience do JWT devem ser definidos.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET deve estar codificado em Base64 válido.", ex);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter no mínimo 256 bits (32 bytes).");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .claim(TOKEN_VERSION_CLAIM, tokenVersionOf(userDetails))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(parseToken(token), userDetails);
    }

    public boolean isTokenValid(Claims claims, UserDetails userDetails) {
        if (claims == null || userDetails == null || !userDetails.isEnabled()) {
            return false;
        }

        String username = claims.getSubject();
        Date expiration = claims.getExpiration();
        long tokenVersion = claims.get(TOKEN_VERSION_CLAIM, Number.class) == null
                ? 0L
                : claims.get(TOKEN_VERSION_CLAIM, Number.class).longValue();

        return username != null
                && username.equalsIgnoreCase(userDetails.getUsername())
                && expiration != null
                && expiration.after(new Date())
                && tokenVersion == tokenVersionOf(userDetails);
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    private long tokenVersionOf(UserDetails userDetails) {
        return userDetails instanceof TokenVersionedUserDetails versioned
                ? versioned.getTokenVersion()
                : 0L;
    }
}
