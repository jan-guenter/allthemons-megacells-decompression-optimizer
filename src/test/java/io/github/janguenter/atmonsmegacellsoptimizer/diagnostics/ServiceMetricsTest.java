package io.github.janguenter.atmonsmegacellsoptimizer.diagnostics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ServiceMetricsTest {
    @Test
    void snapshotTracksRefreshKindsAndTimings() {
        var metrics = new ServiceMetrics();
        metrics.identify("Grid #1");
        metrics.observeScan(2, 7);
        metrics.recordSkippedRefresh();
        metrics.recordRefresh(2_000_000, true, false);
        metrics.recordRefresh(4_000_000, false, true);

        var snapshot = metrics.snapshot();
        assertEquals("Grid #1", snapshot.serviceId());
        assertEquals(1, snapshot.scans());
        assertEquals(2, snapshot.installedModules());
        assertEquals(7, snapshot.patternCount());
        assertEquals(2, snapshot.performedRefreshes());
        assertEquals(1, snapshot.skippedRefreshes());
        assertEquals(1, snapshot.moduleRemovalRefreshes());
        assertEquals(1, snapshot.stockPassThroughRefreshes());
        assertEquals(6_000_000, snapshot.totalRefreshNanos());
        assertEquals(4_000_000, snapshot.maximumRefreshNanos());
        assertEquals(4_000_000, snapshot.lastRefreshNanos());
        assertTrue(snapshot.diagnosticLine().contains("refreshMs(avg/max/last)=3.000/4.000/4.000"));
    }

    @Test
    void resetPreservesLiveStateButClearsCounters() {
        var metrics = new ServiceMetrics();
        metrics.observeScan(1, 3);
        metrics.recordSkippedRefresh();
        metrics.resetCounters();

        var snapshot = metrics.snapshot();
        assertEquals(0, snapshot.scans());
        assertEquals(1, snapshot.installedModules());
        assertEquals(3, snapshot.patternCount());
        assertEquals(0, snapshot.skippedRefreshes());
    }
}
