package com.br.ats.consult.api.job.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationEmbeddable {
    private String city;
    private String state;
    private String country;
}