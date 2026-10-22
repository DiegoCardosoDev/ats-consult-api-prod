package com.br.ats.consult.api.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 255, message = "O email não pode exceder 255 caracteres")
        String email,

        @NotBlank(message = "A password é obrigatória")
        @Size(max = 72, message = "A password não pode exceder 72 caracteres")
        String password
) {}
