package com.mekongmobile.inventory.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks application readiness for the container healthcheck.
 *
 * Spring Boot fires {@link ApplicationReadyEvent} only after every
 * {@code CommandLineRunner} (including the CSV data seeder) has finished.
 * The health endpoint therefore reports HTTP 200/UP only when the schema
 * migration and sample-data load are complete, so Docker Compose never marks
 * the service healthy while the database is still being populated.
 */
@Component
public class StartupReadiness {

    private final AtomicBoolean ready = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        ready.set(true);
    }

    public boolean isReady() {
        return ready.get();
    }
}
