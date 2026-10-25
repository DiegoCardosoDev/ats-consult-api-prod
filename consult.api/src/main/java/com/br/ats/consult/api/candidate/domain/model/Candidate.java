package com.br.ats.consult.api.candidate.domain.model;

import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import com.br.ats.consult.api.technology.domain.model.Technology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    private Long id;
    private String name;
    private Integer age;
    private String role;
    private String linkedinUrl;
    private String email;
    private String phone;
    private EducationLevelEnum educationLevel;
    private EnglishLevelEnum englishLevel;
    @Builder.Default
    private List<Technology> technologies = new ArrayList<>();
    private CandidateAddress address;

    public void normalize() {
        name = trimToNull(name);
        role = trimToNull(role);
        linkedinUrl = trimToNull(linkedinUrl);
        email = trimToNull(email);
        if (email != null) {
            email = email.toLowerCase(Locale.ROOT);
        }
        phone = trimToNull(phone);
        if (technologies == null) {
            technologies = new ArrayList<>();
        }
        if (address != null) {
            address.normalize();
            if (address.getStreet() == null && address.getNumber() == null && address.getComplement() == null
                    && address.getNeighborhood() == null && address.getCity() == null
                    && address.getState() == null && address.getZipCode() == null && address.getCountry() == null) {
                address = null;
            }
        }
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do candidato é obrigatório.");
        }
        if (age == null || age < 16 || age > 120) {
            throw new IllegalArgumentException("A idade do candidato deve estar entre 16 e 120 anos.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("O cargo do candidato é obrigatório.");
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("O e-mail do candidato é inválido.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("O telefone do candidato é obrigatório.");
        }
    }

    public void updateFrom(Candidate source) {
        this.name = source.getName();
        this.age = source.getAge();
        this.role = source.getRole();
        this.linkedinUrl = source.getLinkedinUrl();
        this.email = source.getEmail();
        this.phone = source.getPhone();
        if (source.getEducationLevel() != null) {
            this.educationLevel = source.getEducationLevel();
        }
        if (source.getEnglishLevel() != null) {
            this.englishLevel = source.getEnglishLevel();
        }
        if (source.getTechnologies() != null && !source.getTechnologies().isEmpty()) {
            this.technologies = source.getTechnologies();
        }
        this.address = source.getAddress();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
