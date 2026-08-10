package io.github.janguenter.atmonsmegacellsoptimizer.refresh;

import java.util.List;

public final class RefreshDecision {
    private RefreshDecision() {
    }

    public static boolean shouldRefresh(
            boolean optimizationEnabled,
            boolean stockRefreshWasRequested,
            List<?> previousPatterns,
            List<?> currentPatterns) {
        if (!optimizationEnabled) {
            return stockRefreshWasRequested;
        }
        return !previousPatterns.equals(currentPatterns);
    }
}
