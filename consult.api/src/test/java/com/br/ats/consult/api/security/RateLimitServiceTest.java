package com.br.ats.consult.api.security;

import com.br.ats.consult.api.security.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateLimitServiceTest {

    @Test
    void blocksAttemptsAboveTheConfiguredLimit() {
        RateLimitService service = new RateLimitService();
        ReflectionTestUtils.setField(service, "maxAttempts", 2);
        ReflectionTestUtils.setField(service, "windowSeconds", 60L);
        ReflectionTestUtils.setField(service, "maxEntries", 100);

        service.check("account:test", 2);
        service.check("account:test", 2);

        assertThrows(TooManyRequestsException.class, () -> service.check("account:test", 2));
    }

    @Test
    void resetClearsTheCounter() {
        RateLimitService service = new RateLimitService();
        ReflectionTestUtils.setField(service, "maxAttempts", 1);
        ReflectionTestUtils.setField(service, "windowSeconds", 60L);
        ReflectionTestUtils.setField(service, "maxEntries", 100);

        service.check("account:test");
        service.reset("account:test");

        assertDoesNotThrow(() -> service.check("account:test"));
    }
}
