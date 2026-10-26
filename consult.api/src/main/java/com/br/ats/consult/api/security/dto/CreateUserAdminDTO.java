package com.br.ats.consult.api.security.dto;

import com.br.ats.consult.api.security.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserAdminDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(min = 3, max = 150, message = "O nome completo deve ter entre 3 e 150 caracteres")
        String name,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 255, message = "O email não pode exceder 255 caracteres")
        String email,

        @NotBlank(message = "A password é obrigatória")
        @Size(min = 8, max = 72, message = "A password deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$",
                message = "A password deve conter letras maiúsculas, minúsculas, número e caractere especial"
        )
        String password,

        @NotNull(message = "O perfil (role) é obrigatório")
        Role role
) {
}
