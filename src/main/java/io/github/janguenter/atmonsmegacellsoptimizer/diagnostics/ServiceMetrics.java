package io.github.janguenter.atmonsmegacellsoptimizer.diagnostics;

public final class ServiceMetrics {
    private String serviceId = "unresolved";
    private long scans;
    private int installedModules;
    private int patternCount;
    private long performedRefreshes;
    private long skippedRefreshes;
    private long moduleRemovalRefreshes;
    private long stockPassThroughRefreshes;
    private long totalRefreshNanos;
    private long maximumRefreshNanos;
    private long lastRefreshNanos;
    private long lastLogTick;

    public synchronized void identify(String identity) {
        serviceId = identity;
    }

    public synchronized void observeScan(int modules, int patterns) {
        scans++;
        installedModules = modules;
        patternCount = patterns;
    }

    public synchronized void recordSkippedRefresh() {
        skippedRefreshes++;
    }

    public synchronized void recordRefresh(
            long elapsedNanos,
            boolean moduleRemovalRefresh,
            boolean stockPassThrough) {
        performedRefreshes++;
        if (moduleRemovalRefresh) {
            moduleRemovalRefreshes++;
        }
        if (stockPassThrough) {
            stockPassThroughRefreshes++;
        }
        lastRefreshNanos = elapsedNanos;
        totalRefreshNanos += elapsedNanos;
        maximumRefreshNanos = Math.max(maximumRefreshNanos, elapsedNanos);
    }

    public synchronized boolean shouldLog(long tick, int intervalTicks) {
        if (tick - lastLogTick < intervalTicks) {
            return false;
        }
        lastLogTick = tick;
        return true;
    }

    public synchronized ServiceSnapshot snapshot() {
        return new ServiceSnapshot(
                serviceId,
                scans,
                installedModules,
                patternCount,
                performedRefreshes,
                skippedRefreshes,
                moduleRemovalRefreshes,
                stockPassThroughRefreshes,
                totalRefreshNanos,
                maximumRefreshNanos,
                lastRefreshNanos);
    }

    public synchronized void resetCounters() {
        scans = 0;
        performedRefreshes = 0;
        skippedRefreshes = 0;
        moduleRemovalRefreshes = 0;
        stockPassThroughRefreshes = 0;
        totalRefreshNanos = 0;
        maximumRefreshNanos = 0;
        lastRefreshNanos = 0;
        lastLogTick = 0;
    }
}
