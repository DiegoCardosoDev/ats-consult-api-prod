package br.com.ats.consult.api.selectionprocess.domain.port.out;

import br.com.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import br.com.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import br.com.ats.consult.api.selectionprocess.domain.model.ResumeUpload;

public interface ResumeStorage {

    ResumeMetadata store(ResumeUpload upload);

    ResumeContent load(String storageKey);

    void delete(String storageKey);
}
