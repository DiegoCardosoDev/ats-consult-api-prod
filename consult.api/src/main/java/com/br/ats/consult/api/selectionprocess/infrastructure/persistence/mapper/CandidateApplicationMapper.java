package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper;

import com.br.ats.consult.api.applicationform.domain.model.FormAnswer;
import com.br.ats.consult.api.candidate.infrastructure.persistence.mapper.CandidateMapper;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationAnswerEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CandidateMapper.class})
public interface CandidateApplicationMapper {

    @Mapping(target = "selectionProcessId", source = "process.id")
    @Mapping(target = "resume", source = ".")
    CandidateApplication toDomain(CandidateApplicationEntity entity);

    default ResumeMetadata mapResume(CandidateApplicationEntity entity) {
        return new ResumeMetadata(
                entity.getResumeFilename(),
                entity.getResumeContentType(),
                entity.getResumeSizeBytes(),
                entity.getResumeStorageKey()
        );
    }

    default FormAnswer mapAnswer(CandidateApplicationAnswerEntity entity) {
        return new FormAnswer(
                entity.getQuestionId(),
                entity.getQuestionPrompt(),
                entity.getAnswer(),
                entity.getPosition()
        );
    }
}
