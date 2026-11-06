package br.com.ats.consult.api.security.dto;

import br.com.ats.consult.api.security.model.Role;
import br.com.ats.consult.api.security.model.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean enabled
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
