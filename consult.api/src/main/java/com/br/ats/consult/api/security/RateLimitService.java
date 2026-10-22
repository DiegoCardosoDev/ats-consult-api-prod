package com.br.ats.consult.api.security;

import com.br.ats.consult.api.security.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitService {

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup = new AtomicLong();

    @Value("${app.security.rate-limit.max-attempts:10}")
    private int maxAttempts;

    @Value("${app.security.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Value("${app.security.rate-limit.max-entries:50000}")
    private int maxEntries;

    public void check(String key) {
        check(key, maxAttempts);
    }

    public void check(String key, int limit) {
        long now = Instant.now().getEpochSecond();
        cleanupIfNeeded(now);

        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            if (attempts.size() >= maxEntries) {
                throw new TooManyRequestsException(windowSeconds);
            }
            attempt = attempts.computeIfAbsent(key, ignored -> new Attempt(now));
        }

        synchronized (attempt) {
            long elapsed = now - attempt.windowStart;
            if (elapsed >= windowSeconds) {
                attempt.windowStart = now;
                attempt.count.set(1);
                return;
            }

            int current = attempt.count.incrementAndGet();
            if (current > limit) {
                throw new TooManyRequestsException(windowSeconds - elapsed);
            }
        }
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private void cleanupIfNeeded(long now) {
        long previous = lastCleanup.get();
        if (now - previous < 5 || !lastCleanup.compareAndSet(previous, now)) {
            return;
        }
        attempts.entrySet().removeIf(entry -> now - entry.getValue().windowStart >= windowSeconds);
    }

    private static final class Attempt {
        private long windowStart;
        private final AtomicInteger count = new AtomicInteger();

        private Attempt(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
