package com.br.ats.consult.api.selectionprocess.domain.port.out;

import com.br.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;

public interface ResumeStorage {

    ResumeMetadata store(ResumeUpload upload);

    ResumeContent load(String storageKey);

    void delete(String storageKey);
}
