package br.com.ats.consult.api.security.dto;

public record AuthUserResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
