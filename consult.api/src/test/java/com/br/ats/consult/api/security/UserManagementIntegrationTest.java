package com.br.ats.consult.api.security;

import com.br.ats.consult.api.security.model.Role;
import com.br.ats.consult.api.security.model.User;
import com.br.ats.consult.api.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void publicRegistrationForcesCandidateRoleEvenWhenRoleIsSent() throws Exception {
        String email = uniqueEmail("public");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Candidato Publico","email":"%s","password":"Valid-password1","role":"ADMIN"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getName()).isEqualTo("Candidato Publico");
    }

    @Test
    void publicRegistrationRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Candidato Publico","email":"%s","password":"fraca"}
                                """.formatted(uniqueEmail("weak"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCannotCreateAdminUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserPayload(uniqueEmail("anon"), "ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotCreateAdminUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("regular").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserPayload(uniqueEmail("regular"), "TECH_RECRUITER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateUserWithSelectedRole() throws Exception {
        String email = uniqueEmail("tech");

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserPayload(email, "TECH_RECRUITER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("TECH_RECRUITER"))
                .andExpect(jsonPath("$.name").value("Usuario Interno"));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.TECH_RECRUITER);
    }

    @Test
    void recruiterCanCreateAdminUserAndListUsers() throws Exception {
        String email = uniqueEmail("admin-created");

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("recruiter").roles("TECH_RECRUITER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserPayload(email, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("recruiter").roles("TECH_RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == '%s')]".formatted(email)).exists());
    }

    @Test
    void adminCannotCreateDuplicateEmail() throws Exception {
        String email = uniqueEmail("duplicate");

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserPayload(email, "USER")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserPayload(email, "ADMIN")))
                .andExpect(status().isConflict());
    }

    private String adminUserPayload(String email, String role) {
        return """
                {"name":"Usuario Interno","email":"%s","password":"Valid-password1","role":"%s"}
                """.formatted(email, role);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}
