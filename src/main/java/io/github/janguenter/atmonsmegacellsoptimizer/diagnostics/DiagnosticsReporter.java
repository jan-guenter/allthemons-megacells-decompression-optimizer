package io.github.janguenter.atmonsmegacellsoptimizer.diagnostics;

import io.github.janguenter.atmonsmegacellsoptimizer.AtmonsMegaCellsOptimizer;
import io.github.janguenter.atmonsmegacellsoptimizer.config.OptimizerConfig;

public final class DiagnosticsReporter {
    private DiagnosticsReporter() {
    }

    public static void maybeLog(ServiceMetrics metrics, long tick) {
        if (!OptimizerConfig.VALUES.diagnosticsEnabled.get()) {
            return;
        }
        var interval = OptimizerConfig.VALUES.diagnosticsLogIntervalTicks.get();
        if (metrics.shouldLog(tick, interval)) {
            AtmonsMegaCellsOptimizer.LOGGER.info(
                    "MEGA Cells decompression diagnostics: {}",
                    metrics.snapshot().diagnosticLine());
        }
    }
}
