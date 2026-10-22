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

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        long jobId = createJob("Desenvolvedor Java");

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
        long jobId = createJob("Analista de Dados");

        MvcResult first = applyToJob(jobId, clientId, "Bia Costa", "bia@example.com");
        Number processId = JsonPath.read(first.getResponse().getContentAsString(), "$.selectionProcessId");

        mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate("Bia Costa", "bia@example.com"))
                        .file(pdfFile())
                        .param("clientId", Long.toString(clientId))
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void candidateAppliedToJobCanBeFoundWithAndWithoutSearchFilters() throws Exception {
        long clientId = createClient("11222333000508", "CLI-005");
        long jobId = createJob("Engenheira de Software");

        applyToJob(jobId, clientId, "Carla Souza", "carla@example.com");

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

    private long createJob(String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/job-positions")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","workModel":"REMOTE","contractType":"CLT","status":"OPEN"}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private MvcResult applyToProcess(long processId, String name, String email) throws Exception {
        return mockMvc.perform(multipart("/api/v1/selection-processes/{id}/candidates", processId)
                        .file(jsonCandidate(name, email))
                        .file(pdfFile())
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.selectionProcessId").isNumber())
                .andExpect(jsonPath("$.application.resume.filename").value("curriculo.pdf"))
                .andReturn();
    }

    private MvcResult applyToJob(long jobId, long clientId, String name, String email) throws Exception {
        return mockMvc.perform(multipart("/api/v1/job-positions/{jobId}/applications", jobId)
                        .file(jsonCandidate(name, email))
                        .file(pdfFile())
                        .param("clientId", Long.toString(clientId))
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.selectionProcessId").isNumber())
                .andExpect(jsonPath("$.application.candidate.email").value(email))
                .andReturn();
    }

    private MockMultipartFile jsonCandidate(String name, String email) {
        String json = """
                {
                  "name":"%s",
                  "age":30,
                  "role":"Desenvolvedora Java",
                  "linkedinUrl":"https://www.linkedin.com/in/teste",
                  "email":"%s",
                  "phone":"+351 912 345 678",
                  "address":{"city":"Lisboa","state":"Lisboa","country":"Portugal"}
                }
                """.formatted(name, email);
        return new MockMultipartFile("candidate", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile("resume", "curriculo.pdf", MediaType.APPLICATION_PDF_VALUE, pdfBytes());
    }

    private byte[] pdfBytes() {
        return "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n".getBytes(StandardCharsets.UTF_8);
    }
}
