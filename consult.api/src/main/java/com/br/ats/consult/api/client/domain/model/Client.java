package com.br.ats.consult.api.client.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    private Long id;
    private String name;
    private String cnpj;
    private String code;

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            throw new IllegalArgumentException("O CNPJ deve conter 14 dígitos.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O código do cliente é obrigatório.");
        }
    }

    public void normalize() {
        name = trimToNull(name);
        cnpj = onlyDigits(cnpj);
        code = trimToNull(code);
        if (code != null) {
            code = code.toUpperCase(Locale.ROOT);
        }
    }

    public static String formatCnpj(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            return cnpj;
        }
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8)
                + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    public static String onlyDigits(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
