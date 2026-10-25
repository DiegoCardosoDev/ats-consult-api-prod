package com.br.ats.consult.api.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityConfigValidatorTest {

    @Test
    void rejectsInsecureCookieInProduction() {
        SecurityConfigValidator validator = validator("prod", true, false, "Lax");

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void rejectsDisabledCsrfInProduction() {
        SecurityConfigValidator validator = validator("prod", false, true, "Lax");

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void rejectsSameSiteNoneWithoutSecureCookie() {
        SecurityConfigValidator validator = validator("dev", true, false, "None");

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void acceptsSecureProductionConfiguration() {
        SecurityConfigValidator validator = validator("prod", true, true, "Lax");

        assertDoesNotThrow(validator::validate);
    }

    private SecurityConfigValidator validator(String profile, boolean csrfEnabled, boolean cookieSecure,
                                               String sameSite) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        SecurityConfigValidator validator = new SecurityConfigValidator(environment);
        ReflectionTestUtils.setField(validator, "csrfEnabled", csrfEnabled);
        ReflectionTestUtils.setField(validator, "cookieSecure", cookieSecure);
        ReflectionTestUtils.setField(validator, "cookieSameSite", sameSite);
        return validator;
    }
}
