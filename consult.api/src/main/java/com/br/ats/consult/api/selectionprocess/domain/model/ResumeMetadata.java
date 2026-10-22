package com.br.ats.consult.api.selectionprocess.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeMetadata {

    private String filename;
    private String contentType;
    private long sizeBytes;
    private String storageKey;
}
