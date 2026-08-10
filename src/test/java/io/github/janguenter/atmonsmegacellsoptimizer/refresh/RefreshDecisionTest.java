package io.github.janguenter.atmonsmegacellsoptimizer.refresh;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RefreshDecisionTest {
    @Test
    void unchangedPatternsSkipTheExpensiveRefresh() {
        assertFalse(RefreshDecision.shouldRefresh(
                true,
                true,
                List.of("iron", "gold"),
                List.of("iron", "gold")));
    }

    @Test
    void additionsRemovalsAndOrderChangesRefresh() {
        assertTrue(RefreshDecision.shouldRefresh(true, true, List.of("iron"), List.of("iron", "gold")));
        assertTrue(RefreshDecision.shouldRefresh(true, true, List.of("iron"), List.of()));
        assertTrue(RefreshDecision.shouldRefresh(
                true,
                true,
                List.of("iron", "gold"),
                List.of("gold", "iron")));
    }

    @Test
    void lastModuleRemovalRefreshesAPreviouslyNonemptyProvider() {
        assertTrue(RefreshDecision.shouldRefresh(true, false, List.of("iron"), List.of()));
        assertFalse(RefreshDecision.shouldRefresh(true, false, List.of(), List.of()));
    }

    @Test
    void disabledModePreservesStockCallSites() {
        assertTrue(RefreshDecision.shouldRefresh(false, true, List.of("iron"), List.of("iron")));
        assertFalse(RefreshDecision.shouldRefresh(false, false, List.of("iron"), List.of()));
    }
}
