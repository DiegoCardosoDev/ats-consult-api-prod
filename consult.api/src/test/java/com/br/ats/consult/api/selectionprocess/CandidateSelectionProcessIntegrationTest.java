package com.br.ats.consult.api.selectionprocess;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateSelectionProcessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void candidateAppliesWithPdfAndCanBeSearchedAndDownloaded() throws Exception {
        long clientId = createClient("11222333000181", "CLI-001");
        long jobId = createJob("Desenvolvedor Java", clientId);

        MvcResult processCreated = mockMvc.perform(post("/api/v1/selection-processes")
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"jobPositionId":%d,"status":"OPEN","openedAt":"2026-10-22"}
                                """.formatted(clientId, jobId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.client.id").value(clientId))
                .andExpect(jsonPath("$.jobPosition.id").value(jobId))
                .andReturn();

        Number processId = JsonPath.read(processCreated.getResponse().getContentAsString(), "$.id");

        MvcResult applied = applyToProcess(processId.longValue(), "Ana Silva", "ana@example.com");
        Number applicationId = JsonPath.read(applied.getResponse().getContentAsString(), "$.application.id");

        mockMvc.perform(get("/api/v1/candidates")
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .param("name", "Ana")
                        .param("role", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("Desenvolvedora Java"));

        mockMvc.perform(get("/api/v1/selection-processes/{id}", processId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates[0].id").value(applicationId.intValue()));

        mockMvc.perform(get("/api/v1/applications/{id}/resume", applicationId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdfBytes()));
    }

    @Test
    void applyingToJobCreatesOrReusesOpenProcessAndPreventsDuplicates() throws Exception {
        long clientId = createClient("11222333000262", "CLI-002");
        long jobId = createJob("Analista de Dados", clientId);

        MvcResult first = applyToJob(jobId, "Bia Costa", "bia@example.com");
        Number processId = JsonPath.read(first.getResponse().getContentAsString(), "$.selectionProcessId");

        mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate("Bia Costa", "bia@example.com"))
                        .file(pdfFile())
                        .file(consentFile())
                        .file(answersFile(jobId, "Bia Costa", "bia@example.com"))
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void candidateAppliedToJobCanBeFoundWithAndWithoutSearchFilters() throws Exception {
        long clientId = createClient("11222333000508", "CLI-005");
        long jobId = createJob("Engenheira de Software", clientId);

        applyToJob(jobId, "Carla Souza", "carla@example.com");

        mockMvc.perform(get("/api/v1/candidates")
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .param("name", "Carla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", hasItem("carla@example.com")));

        mockMvc.perform(get("/api/v1/candidates")
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", hasItem("carla@example.com")));
    }

    @Test
    void jobApplicationFormIsAssociatedWithJobAndAnswersAreStored() throws Exception {
        long clientId = createClient("11222333000661", "CLI-006");
        long jobId = createJob("Vaga com Formulário", clientId);
        long otherJobId = createJob("Vaga Sem Formulário", clientId);

        mockMvc.perform(get("/api/v1/job-positions/{jobId}/application-form", jobId)
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions.length()", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.questions[0].questionKey").value("FULL_NAME"));

        mockMvc.perform(put("/api/v1/job-positions/{jobId}/application-form", jobId)
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Perguntas da vaga",
                                  "description":"Responda antes de enviar a candidatura.",
                                  "active":true,
                                  "questions":[
                                    {"prompt":"Tem experiência com Java?","type":"YES_NO","required":true,"options":[],"position":0},
                                    {"prompt":"Qual o seu nível?","type":"SINGLE_CHOICE","required":true,"options":["Júnior","Pleno","Sênior"],"position":1}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobPositionId").value(jobId))
                .andExpect(jsonPath("$.questions.length()", greaterThanOrEqualTo(7)))
                .andExpect(jsonPath("$.questions[0].questionKey").value("FULL_NAME"))
                .andExpect(jsonPath("$.questions[1].questionKey").value("AGE"))
                .andExpect(jsonPath("$.questions[2].questionKey").value("ROLE"))
                .andExpect(jsonPath("$.questions[3].questionKey").value("EMAIL"))
                .andExpect(jsonPath("$.questions[4].questionKey").value("PHONE"))
                .andExpect(jsonPath("$.questions[*].prompt", hasItem("Tem experiência com Java?")))
                .andExpect(jsonPath("$.questions[*].prompt", hasItem("Qual o seu nível?")));

        mockMvc.perform(get("/api/v1/job-positions/{jobId}/application-form", otherJobId)
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions.length()", greaterThanOrEqualTo(5)));

        mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate("Diego Formulário", "diego.formulario@example.com"))
                        .file(pdfFile())
                        .file(consentFile())
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        MvcResult applied = applyToJob(jobId, "Diego Formulário", "diego.formulario@example.com");
        Number processId = JsonPath.read(applied.getResponse().getContentAsString(), "$.selectionProcessId");

        MvcResult processResult = mockMvc.perform(get("/api/v1/selection-processes/{id}", processId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates[0].answers.length()", greaterThanOrEqualTo(7)))
                .andReturn();

        List<Map<String, Object>> storedAnswers = JsonPath.read(
                processResult.getResponse().getContentAsString(), "$.candidates[0].answers");
        assertEquals("Sim", answerForPrompt(storedAnswers, "Tem experiência com Java?"));
        assertEquals("Júnior", answerForPrompt(storedAnswers, "Qual o seu nível?"));
    }

    @Test
    void missingApplicationFormIsGeneratedWhenOpeningJobAndWhenApplying() throws Exception {
        long clientId = createClient("11222333001118", "CLI-011");
        long jobId = createJob("Vaga Sem Formulário Persistido", clientId);

        mockMvc.perform(delete("/api/v1/job-positions/{jobId}/application-form", jobId)
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate("Sem Formulário", "sem.formulario@example.com"))
                        .file(pdfFile())
                        .file(consentFile())
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/job-positions/{jobId}/application-form", jobId)
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.questions.length()", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.questions[0].questionKey").value("FULL_NAME"));

        MvcResult applied = applyToJob(jobId, "Com Formulário", "com.formulario@example.com");
        Number processId = JsonPath.read(applied.getResponse().getContentAsString(), "$.selectionProcessId");

        mockMvc.perform(get("/api/v1/selection-processes/{id}", processId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates[0].answers.length()", greaterThanOrEqualTo(5)));
    }

    @Test
    void generatedFormGroupsTechnologyQuestionsAndEnrichesCandidateProfile() throws Exception {
        long clientId = createClient("11222333000823", "CLI-008");
        long awsId = createTechnology("AWS", "CLOUD_PROVIDER", null);
        createTechnology("S3", "STORAGE", awsId);
        createTechnology("Lambda", "SERVERLESS", awsId);
        long javaId = createTechnology("Java", "LANGUAGE", null);
        long kotlinId = createTechnology("Kotlin", "LANGUAGE", null);
        long jobId = createJobWithTechnologies("Vaga Cloud", clientId, awsId, javaId, kotlinId);

        MvcResult formResult = mockMvc.perform(
                        get("/api/v1/job-positions/{jobId}/application-form", jobId)
                                .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[*].questionKey", hasItem("TECH_CATEGORY_LANGUAGE")))
                .andExpect(jsonPath("$.questions[*].questionKey", hasItem("TECH_CHILDREN_" + awsId)))
                .andExpect(jsonPath("$.questions[*].questionKey", hasItem("EDUCATION_LEVEL")))
                .andReturn();

        List<Map<String, Object>> questions = JsonPath.read(
                formResult.getResponse().getContentAsString(), "$.questions");
        StringBuilder answers = new StringBuilder("{\"answers\":[");
        boolean first = true;
        for (Map<String, Object> question : questions) {
            String key = String.valueOf(question.get("questionKey"));
            String type = String.valueOf(question.get("type"));
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) question.get("options");
            String answer = switch (key) {
                case "FULL_NAME" -> "Perfil Enriquecido";
                case "AGE" -> "30";
                case "ROLE" -> "Desenvolvedor";
                case "EMAIL" -> "perfil.enriquecido@example.com";
                case "PHONE" -> "+351 912 345 678";
                case "LINKEDIN_URL" -> "https://www.linkedin.com/in/perfil";
                case "SALARY_EXPECTATION" -> "9000";
                case "WORK_MODEL" -> "REMOTE; HYBRID";
                case "EDUCATION_LEVEL" -> "BACHELORS";
                case "ENGLISH_LEVEL" -> "ADVANCED";
                case "TECH_CATEGORY_LANGUAGE" -> "Java";
                default -> {
                    if (key.startsWith("TECH_CHILDREN_")) {
                        yield "S3; Lambda";
                    }
                    if (Boolean.TRUE.equals(question.get("required"))) {
                        if ("NUMBER".equals(type)) {
                            yield "1";
                        }
                        yield options.isEmpty() ? "Sim" : options.get(0);
                    }
                    yield null;
                }
            };
            if (answer == null) {
                continue;
            }
            if (!first) {
                answers.append(',');
            }
            first = false;
            answers.append("{\"questionId\":").append(question.get("id"))
                    .append(",\"answer\":\"").append(answer).append("\"}");
        }
        answers.append("]}");

        MvcResult applied = mockMvc.perform(
                        multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                                .file(jsonCandidate("Perfil Enriquecido", "perfil.enriquecido@example.com", kotlinId))
                                .file(pdfFile())
                        .file(consentFile())
                                .file(new MockMultipartFile("answers", "", MediaType.APPLICATION_JSON_VALUE,
                                        answers.toString().getBytes(StandardCharsets.UTF_8)))
                                .with(user("recruiter").roles("TECH_RECRUITER"))
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.application.adherence.percentage").isNumber())
                .andExpect(jsonPath("$.application.adherence.criteria[0].key").value("TECHNOLOGIES"))
                .andExpect(jsonPath("$.application.adherence.criteria[0].percentage").value(100))
                .andReturn();

        Number candidateId = JsonPath.read(
                applied.getResponse().getContentAsString(), "$.application.candidate.id");
        Number selectionProcessId = JsonPath.read(
                applied.getResponse().getContentAsString(), "$.selectionProcessId");

        mockMvc.perform(get("/api/v1/selection-processes/{id}", selectionProcessId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates[*].answers[*].questionKey", hasItem("WORK_MODEL")))
                .andExpect(jsonPath("$.candidates[*].answers[*].questionKey", hasItem("EDUCATION_LEVEL")))
                .andExpect(jsonPath("$.candidates[0].adherence.percentage").isNumber())
                .andExpect(jsonPath("$.candidates[0].adherence.criteria[0].key").value("TECHNOLOGIES"))
                .andExpect(jsonPath("$.candidates[0].adherence.criteria[0].percentage").value(100))
                .andExpect(jsonPath("$.candidates[0].candidate.technologies[*].name", hasItem("AWS")))
                .andExpect(jsonPath("$.candidates[0].adherence.criteria[*].key", hasItem("WORK_MODEL")));

        mockMvc.perform(get("/api/v1/candidates/{id}", candidateId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.educationLevel").value("BACHELORS"))
                .andExpect(jsonPath("$.englishLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.technologies[*].name", hasItem("Java")))
                .andExpect(jsonPath("$.technologies[*].name", hasItem("Kotlin")))
                .andExpect(jsonPath("$.technologies[*].name", hasItem("S3")))
                .andExpect(jsonPath("$.technologies[*].name", hasItem("Lambda")));
    }

    @Test
    void applicationRequiresPrivacyConsent() throws Exception {
        long clientId = createClient("11222333000912", "CLI-010");
        long jobId = createJob("Vaga LGPD", clientId);

        mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate("Sem Consentimento", "sem.consentimento@example.com"))
                        .file(pdfFile())
                        .file(answersFile(jobId, "Sem Consentimento", "sem.consentimento@example.com"))
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("É obrigatório aceitar a Política de Privacidade para enviar a candidatura."));
    }

    @Test
    void kanbanStatusAndCommentsAreUpdatedAndListedNewestFirst() throws Exception {
        long clientId = createClient("11222333000742", "CLI-007");
        long jobId = createJob("Vaga Kanban", clientId);

        MvcResult processCreated = mockMvc.perform(post("/api/v1/selection-processes")
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"jobPositionId":%d,"status":"OPEN","openedAt":"2026-10-22"}
                                """.formatted(clientId, jobId)))
                .andExpect(status().isCreated())
                .andReturn();
        Number processId = JsonPath.read(processCreated.getResponse().getContentAsString(), "$.id");

        MvcResult applied = applyToProcess(processId.longValue(), "Eva Kanban", "eva.kanban@example.com");
        Number applicationId = JsonPath.read(applied.getResponse().getContentAsString(), "$.application.id");

        mockMvc.perform(patch("/api/v1/applications/{id}/status", applicationId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ENTREVISTA_TECH_RECRUITER"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREVISTA_TECH_RECRUITER"));

        mockMvc.perform(post("/api/v1/applications/{id}/comments", applicationId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Primeiro contacto realizado."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorEmail").value("recruiter"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        mockMvc.perform(post("/api/v1/applications/{id}/comments", applicationId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Entrevista agendada."}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/applications/{id}/comments", applicationId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Entrevista agendada."))
                .andExpect(jsonPath("$[1].message").value("Primeiro contacto realizado."));

        mockMvc.perform(get("/api/v1/selection-processes/{id}", processId.longValue())
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates[0].status").value("ENTREVISTA_TECH_RECRUITER"))
                .andExpect(jsonPath("$.candidates[0].comments.length()").value(2))
                .andExpect(jsonPath("$.candidates[0].comments[0].message").value("Entrevista agendada."));
    }

    @Test
    void regularUserCannotCreateClients() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cliente","cnpj":"11222333000343","code":"CLI-003"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createsClientWithGeneratedCodeWhenCodeIsOmitted() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clients")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cliente Automático","cnpj":"11222333000416"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("11.222.333/0004-16"))
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andReturn();

        String code = JsonPath.read(result.getResponse().getContentAsString(), "$.code");
        assertTrue(code.startsWith("CLI-"));
        assertEquals(12, code.length());
    }

    private long createClient(String cnpj, String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clients")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cliente Teste","cnpj":"%s","code":"%s"}
                                """.formatted(cnpj, code)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createJob(String title, long clientId) throws Exception {
        return createJobWithTechnologies(title, clientId);
    }

    private long createJobWithTechnologies(String title, long clientId, long... technologyIds) throws Exception {
        String ids = java.util.Arrays.stream(technologyIds)
                .mapToObj(Long::toString)
                .collect(java.util.stream.Collectors.joining(","));
        MvcResult result = mockMvc.perform(post("/api/v1/job-positions")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"title":"%s","workModel":"REMOTE","contractType":"CLT",
                                 "status":"OPEN","technologyIds":[%s]}
                                """.formatted(clientId, title, ids)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createTechnology(String name, String category, Long parentId) throws Exception {
        String parent = parentId == null ? "null" : parentId.toString();
        MvcResult result = mockMvc.perform(post("/api/v1/technologies")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","category":"%s","parentId":%s}
                                """.formatted(name, category, parent)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private MvcResult applyToProcess(long processId, String name, String email) throws Exception {
        MvcResult process = mockMvc.perform(get("/api/v1/selection-processes/{id}", processId)
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andReturn();
        Number jobId = JsonPath.read(process.getResponse().getContentAsString(), "$.jobPosition.id");

        return mockMvc.perform(multipart("/api/v1/selection-processes/{id}/candidates", processId)
                        .file(jsonCandidate(name, email))
                        .file(pdfFile())
                        .file(consentFile())
                        .file(answersFile(jobId.longValue(), name, email))
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.selectionProcessId").isNumber())
                .andExpect(jsonPath("$.application.resume.filename").value("curriculo.pdf"))
                .andExpect(jsonPath("$.application.consent.accepted").value(true))
                .andExpect(jsonPath("$.application.consent.version").value("1.0"))
                .andExpect(jsonPath("$.application.consent.acceptedAt").isNotEmpty())
                .andReturn();
    }

    private String answerForPrompt(List<Map<String, Object>> answers, String prompt) {
        return answers.stream()
                .filter(answer -> prompt.equals(answer.get("questionPrompt")))
                .map(answer -> String.valueOf(answer.get("answer")))
                .findFirst()
                .orElse(null);
    }

    private MvcResult applyToJob(long jobId, String name, String email) throws Exception {
        return mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate(name, email))
                        .file(pdfFile())
                        .file(consentFile())
                        .file(answersFile(jobId, name, email))
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.selectionProcessId").isNumber())
                .andExpect(jsonPath("$.application.candidate.email").value(email))
                .andExpect(jsonPath("$.application.consent.accepted").value(true))
                .andExpect(jsonPath("$.application.consent.version").value("1.0"))
                .andExpect(jsonPath("$.application.consent.acceptedAt").isNotEmpty())
                .andReturn();
    }

    private MockMultipartFile answersFile(long jobId, String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/job-positions/{jobId}/application-form", jobId)
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andReturn();

        List<Map<String, Object>> questions = JsonPath.read(
                result.getResponse().getContentAsString(), "$.questions");
        StringBuilder answers = new StringBuilder("{\"answers\":[");
        boolean first = true;
        for (Map<String, Object> question : questions) {
            if (!Boolean.TRUE.equals(question.get("required"))) {
                continue;
            }
            String key = String.valueOf(question.get("questionKey"));
            String type = String.valueOf(question.get("type"));
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) question.get("options");
            String answer = switch (key) {
                case "FULL_NAME" -> name;
                case "AGE" -> "30";
                case "ROLE" -> "Desenvolvedora Java";
                case "EMAIL" -> email;
                case "PHONE" -> "+351 912 345 678";
                default -> {
                    if ("TEXT".equals(type) || "TEXTAREA".equals(type)) {
                        yield "Resposta de teste";
                    }
                    if ("NUMBER".equals(type)) {
                        yield "5000";
                    }
                    if ("YES_NO".equals(type)) {
                        yield "Sim";
                    }
                    if ("MULTIPLE_CHOICE".equals(type)) {
                        Object maxSelections = question.get("maxSelections");
                        int limit = maxSelections instanceof Number number ? number.intValue() : 1;
                        yield String.join("; ", options.stream().limit(limit).toList());
                    }
                    yield options.isEmpty() ? "" : options.get(0);
                }
            };
            if (!first) {
                answers.append(',');
            }
            first = false;
            answers.append("{\"questionId\":").append(question.get("id"))
                    .append(",\"answer\":\"").append(answer).append("\"}");
        }
        answers.append("]}");

        return new MockMultipartFile("answers", "", MediaType.APPLICATION_JSON_VALUE,
                answers.toString().getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile jsonCandidate(String name, String email) {
        return jsonCandidate(name, email, new Long[0]);
    }

    private MockMultipartFile jsonCandidate(String name, String email, Long... technologyIds) {
        String technologyIdList = java.util.Arrays.stream(technologyIds)
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        String technologyField = technologyIds.length == 0
                ? ""
                : ",\n  \"technologyIds\":[%s]".formatted(technologyIdList);
        String json = """
                {
                  "name":"%s",
                  "age":30,
                  "role":"Desenvolvedora Java",
                  "linkedinUrl":"https://www.linkedin.com/in/teste",
                  "email":"%s",
                  "phone":"+351 912 345 678"%s,
                  "address":{"city":"Lisboa","state":"Lisboa","country":"Portugal"}
                }
                """.formatted(name, email, technologyField);
        return new MockMultipartFile("candidate", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile consentFile() {
        String json = """
                {"accepted":true,"version":"1.0"}
                """;
        return new MockMultipartFile("consent", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile("resume", "curriculo.pdf", MediaType.APPLICATION_PDF_VALUE, pdfBytes());
    }

    private byte[] pdfBytes() {
        return "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n".getBytes(StandardCharsets.UTF_8);
    }
}
