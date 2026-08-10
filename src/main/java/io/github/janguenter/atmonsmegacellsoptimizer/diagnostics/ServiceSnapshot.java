package io.github.janguenter.atmonsmegacellsoptimizer.diagnostics;

import java.util.Locale;

public record ServiceSnapshot(
        String serviceId,
        long scans,
        int installedModules,
        int patternCount,
        long performedRefreshes,
        long skippedRefreshes,
        long moduleRemovalRefreshes,
        long stockPassThroughRefreshes,
        long totalRefreshNanos,
        long maximumRefreshNanos,
        long lastRefreshNanos) {

    public String diagnosticLine() {
        var averageMillis = performedRefreshes == 0
                ? 0.0
                : totalRefreshNanos / 1_000_000.0 / performedRefreshes;
        return String.format(
                Locale.ROOT,
                "%s scans=%d modules=%d patterns=%d refreshes=%d skipped=%d removalFix=%d stock=%d "
                        + "refreshMs(avg/max/last)=%.3f/%.3f/%.3f",
                serviceId,
                scans,
                installedModules,
                patternCount,
                performedRefreshes,
                skippedRefreshes,
                moduleRemovalRefreshes,
                stockPassThroughRefreshes,
                averageMillis,
                maximumRefreshNanos / 1_000_000.0,
                lastRefreshNanos / 1_000_000.0);
    }
}
