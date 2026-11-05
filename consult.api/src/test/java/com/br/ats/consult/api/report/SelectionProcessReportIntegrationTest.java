package com.br.ats.consult.api.report;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SelectionProcessReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void staffCanGenerateProcessReport() throws Exception {
        mockMvc.perform(get("/api/v1/reports/selection-processes")
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalProcesses").isNumber())
                .andExpect(jsonPath("$.summary.totalApplications").isNumber())
                .andExpect(jsonPath("$.summary.inTriage").isNumber())
                .andExpect(jsonPath("$.summary.rejected").isNumber())
                .andExpect(jsonPath("$.summary.approvedByClient").isNumber())
                .andExpect(jsonPath("$.summary.hired").isNumber())
                .andExpect(jsonPath("$.processes.content").isArray());
    }

    @Test
    void reportAcceptsFilters() throws Exception {
        mockMvc.perform(get("/api/v1/reports/selection-processes")
                        .param("status", "OPEN")
                        .param("clientId", "999999")
                        .param("jobPositionId", "999999")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalProcesses").value(0))
                .andExpect(jsonPath("$.summary.totalApplications").value(0));
    }

    @Test
    void candidatesCannotAccessReport() throws Exception {
        mockMvc.perform(get("/api/v1/reports/selection-processes")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffCanOpenProcessDashboard() throws Exception {
        long clientId = createClient("99888777000177", "CLI-DASH");
        long jobId = createJob(clientId);
        long processId = createProcess(clientId, jobId);

        mockMvc.perform(get("/api/v1/reports/selection-processes/{processId}", processId)
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processId").value(processId))
                .andExpect(jsonPath("$.client.id").value(clientId))
                .andExpect(jsonPath("$.jobPosition.id").value(jobId))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.totalApplications").value(0))
                .andExpect(jsonPath("$.hired").value(0))
                .andExpect(jsonPath("$.averageAdherence").value(0))
                .andExpect(jsonPath("$.conversionRate").value(0.0))
                .andExpect(jsonPath("$.statusCounts.TRIAGEM").value(0))
                .andExpect(jsonPath("$.statusCounts.STAND_BY").value(0))
                .andExpect(jsonPath("$.timeline").isArray())
                .andExpect(jsonPath("$.topCandidates").isArray());
    }

    @Test
    void dashboardForMissingProcessReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/reports/selection-processes/999999")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void candidatesCannotAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/reports/selection-processes/1")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private long createClient(String cnpj, String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clients")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cliente Dashboard","cnpj":"%s","code":"%s"}
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
                                {"clientId":%d,"title":"Vaga Dashboard","workModel":"REMOTE","contractType":"CLT","status":"OPEN"}
                                """.formatted(clientId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createProcess(long clientId, long jobId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/selection-processes")
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"jobPositionId":%d,"status":"OPEN","openedAt":"2026-11-01"}
                                """.formatted(clientId, jobId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
