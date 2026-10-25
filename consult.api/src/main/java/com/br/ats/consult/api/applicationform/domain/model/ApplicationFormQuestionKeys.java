package com.br.ats.consult.api.applicationform.domain.model;

import java.util.List;

public final class ApplicationFormQuestionKeys {

    public static final String FULL_NAME = "FULL_NAME";
    public static final String AGE = "AGE";
    public static final String ROLE = "ROLE";
    public static final String EMAIL = "EMAIL";
    public static final String PHONE = "PHONE";
    public static final String LINKEDIN_URL = "LINKEDIN_URL";
    public static final String STREET = "STREET";
    public static final String ADDRESS_NUMBER = "ADDRESS_NUMBER";
    public static final String COMPLEMENT = "COMPLEMENT";
    public static final String NEIGHBORHOOD = "NEIGHBORHOOD";
    public static final String CITY = "CITY";
    public static final String STATE = "STATE";
    public static final String ZIP_CODE = "ZIP_CODE";
    public static final String COUNTRY = "COUNTRY";
    public static final String SALARY_EXPECTATION = "SALARY_EXPECTATION";
    public static final String WORK_MODEL = "WORK_MODEL";
    public static final String EDUCATION_LEVEL = "EDUCATION_LEVEL";
    public static final String ENGLISH_LEVEL = "ENGLISH_LEVEL";
    public static final String TECHNOLOGY_CATEGORY_PREFIX = "TECH_CATEGORY_";
    public static final String TECHNOLOGY_CHILDREN_PREFIX = "TECH_CHILDREN_";
    public static final List<String> CORE_KEYS = List.of(FULL_NAME, AGE, ROLE, EMAIL, PHONE);

    private ApplicationFormQuestionKeys() {
    }
}
