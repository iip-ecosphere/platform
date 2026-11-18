package de.iip_ecosphere.platform.examples.energy.aggregator.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-source health: last successful reading, last error, counters.
 */
public class SourceHealthMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(SourceHealthMonitor.class);

    public static class SourceStatus {
        public volatile boolean connected;
        public volatile long lastSuccessfulReadingMs;
        public volatile Throwable lastError;
        public volatile long totalReadings;
        public volatile long failedReadings;
    }

    private final Map<String, SourceStatus> statuses = new ConcurrentHashMap<>();

    public SourceStatus getOrCreate(String sourceId) {
        return statuses.computeIfAbsent(sourceId, k -> new SourceStatus());
    }

    public void recordSuccess(String sourceId) {
        SourceStatus s = getOrCreate(sourceId);
        s.connected = true;
        s.lastSuccessfulReadingMs = Instant.now().toEpochMilli();
        s.totalReadings++;
    }

    public void recordFailure(String sourceId, Throwable t) {
        SourceStatus s = getOrCreate(sourceId);
        s.connected = false;
        s.lastError = t;
        s.failedReadings++;
        LOG.warn("[{}] Source failure (total failures: {})", sourceId, s.failedReadings, t);
    }

    public Map<String, SourceStatus> snapshot() {
        return statuses;
    }
}
