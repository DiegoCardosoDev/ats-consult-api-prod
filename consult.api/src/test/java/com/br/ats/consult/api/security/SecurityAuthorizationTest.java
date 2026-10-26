package com.br.ats.consult.api.security;

import com.br.ats.consult.api.security.model.Role;
import com.br.ats.consult.api.security.model.User;
import com.br.ats.consult.api.security.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void jobPositionsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/job-positions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownGetRouteIsNotPublic() throws Exception {
        mockMvc.perform(get("/internal/secret"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void csrfEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginCreatesServerSideSessionAndCurrentUserEndpointResolvesSession() throws Exception {
        String email = "session-user@example.com";
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("valid-password"))
                .role(Role.TECH_RECRUITER)
                .build());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"valid-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);
        assertNull(login.getResponse().getCookie("auth_token"));

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("TECH_RECRUITER"));
    }

    @Test
    void logoutInvalidatesServerSideSession() throws Exception {
        String email = "logout-user@example.com";
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("valid-password"))
                .role(Role.USER)
                .build());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"valid-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk());

        assertTrue(session.isInvalid());
    }

    @Test
    void loginRateLimitReturnsTooManyRequests() throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"ratelimit@example.com","password":"invalid-password"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ratelimit@example.com","password":"invalid-password"}
                                """))
                .andExpect(status().isTooManyRequests());
    }


    @Test
    void regularUserCanReadJobsButCannotListManagementAreas() throws Exception {
        mockMvc.perform(get("/api/v1/job-positions")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/clients")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/candidates")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/selection-processes")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotManageApplicationFormsOrApplications() throws Exception {
        mockMvc.perform(put("/api/v1/job-positions/1/application-form")
                        .with(user("candidate").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Formulário","active":true,"questions":[]}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/job-positions/1/application-form")
                        .with(user("candidate").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/applications/1")
                        .with(user("candidate").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/applications/1/comments")
                        .with(user("candidate").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Comentário"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotCreateJobPosition() throws Exception {
        mockMvc.perform(post("/api/v1/job-positions")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Vaga","workModel":"REMOTE","contractType":"CLT","status":"OPEN"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAndUpdateJobPositionPreservingStatus() throws Exception {
        long clientId = createClient("11222333000904", "CLI-009");

        MvcResult created = mockMvc.perform(post("/api/v1/job-positions")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"title":"Vaga original","workModel":"REMOTE","contractType":"CLT","status":"OPEN"}
                                """.formatted(clientId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.client.id").value(clientId))
                .andReturn();

        Number id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/v1/job-positions/" + id)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":%d,"title":"Vaga atualizada","status":"OPEN","workModel":"HYBRID","contractType":"PJ"}
                                """.formatted(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Vaga atualizada"))
                .andExpect(jsonPath("$.status").value("OPEN"));
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

    @Test
    void updateWithoutStatusIsRejected() throws Exception {
        mockMvc.perform(put("/api/v1/job-positions/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Vaga atualizada","workModel":"HYBRID","contractType":"PJ"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingJobPositionReturnsStandardNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/job-positions/999999")
                        .with(user("user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void invalidTitleIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/job-positions")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","workModel":"REMOTE","contractType":"CLT","status":"OPEN"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
