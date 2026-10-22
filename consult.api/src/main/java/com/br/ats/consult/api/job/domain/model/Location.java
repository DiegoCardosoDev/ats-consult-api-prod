package com.br.ats.consult.api.job.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String city;
    private String state;
    private String country;
}