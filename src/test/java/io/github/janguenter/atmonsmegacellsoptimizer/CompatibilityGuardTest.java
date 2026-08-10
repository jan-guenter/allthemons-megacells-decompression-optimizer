package io.github.janguenter.atmonsmegacellsoptimizer;

import org.junit.jupiter.api.Test;

class CompatibilityGuardTest {
    @Test
    void exactTargetSignaturesRemainAvailable() {
        CompatibilityGuard.verifySignatures();
    }
}
