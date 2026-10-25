package com.br.ats.consult.api.security.dto;

public record AuthUserResponse(
        Long id,
        String email,
        String role
) {
}
