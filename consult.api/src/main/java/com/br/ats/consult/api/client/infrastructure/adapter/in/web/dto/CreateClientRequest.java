package com.br.ats.consult.api.client.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank(message = "O nome do cliente é obrigatório")
        @Size(max = 150, message = "O nome do cliente não pode exceder 150 caracteres")
        String name,
        @NotBlank(message = "O CNPJ é obrigatório")
        @Size(max = 20, message = "O CNPJ é inválido")
        @Pattern(regexp = "^[\\d./-]+$", message = "O CNPJ contém caracteres inválidos")
        String cnpj,
        @Size(max = 50, message = "O código do cliente não pode exceder 50 caracteres")
        String code
) {
}
