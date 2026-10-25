package com.br.ats.consult.api.applicationform.infrastructure.persistence.entity;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_application_form_questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFormQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    private ApplicationFormEntity form;

    @Column(nullable = false, length = 500)
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionTypeEnum type;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "question_order", nullable = false)
    private Integer position;

    @Column(name = "question_key", length = 100)
    private String questionKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "technology_category", length = 50)
    private TechnologyCategoryEnum technologyCategory;

    @Column(name = "parent_technology_id")
    private Long parentTechnologyId;

    @Column(name = "max_selections")
    private Integer maxSelections;

    @Column(name = "system_generated", nullable = false)
    private boolean systemGenerated;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tb_application_form_question_options",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @OrderColumn(name = "option_order")
    @Column(name = "option_value", length = 255)
    @Builder.Default
    private List<String> options = new ArrayList<>();
}
