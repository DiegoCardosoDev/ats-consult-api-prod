package br.com.ats.consult.api.job;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobPositionStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void closeFreezeAndReopenRequireReasonAndClearItOnReopen() throws Exception {
        long clientId = createClient("99888777000166", "CLI-STATUS");
        long jobId = createJob(clientId);

        mockMvc.perform(patch("/api/v1/job-positions/{id}/status", jobId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"FROZEN"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/job-positions/{id}/status", jobId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"FROZEN","reason":"Aguardando definição do cliente"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"))
                .andExpect(jsonPath("$.statusReason").value("Aguardando definição do cliente"));

        mockMvc.perform(patch("/api/v1/job-positions/{id}/status", jobId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"OPEN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(patch("/api/v1/job-positions/{id}/status", jobId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CLOSED","reason":"Vaga preenchida"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.statusReason").value("Vaga preenchida"));

        mockMvc.perform(patch("/api/v1/job-positions/{id}/status", jobId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"OPEN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    private long createClient(String cnpj, String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clients")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cliente Status","cnpj":"%s","code":"%s"}
                                """.formatted(cnpj, code)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createJob(long clientId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/job-positions")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"title":"Vaga Status","workModel":"REMOTE","contractType":"CLT","status":"OPEN"}
                                """.formatted(clientId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
