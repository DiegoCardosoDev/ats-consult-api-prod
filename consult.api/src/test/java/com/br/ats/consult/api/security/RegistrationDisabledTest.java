package com.br.ats.consult.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.registration.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicRegistrationIsForbiddenWhenDisabled() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Novo Usuario","email":"new-user@example.com","password":"Valid-password1"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("O auto-registo está desativado."));
    }
}
