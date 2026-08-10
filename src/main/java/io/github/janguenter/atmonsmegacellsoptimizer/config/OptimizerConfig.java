package io.github.janguenter.atmonsmegacellsoptimizer.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class OptimizerConfig {
    public static final Values VALUES;
    private static final ModConfigSpec SPEC;

    static {
        var configured = new ModConfigSpec.Builder().configure(Values::new);
        VALUES = configured.getLeft();
        SPEC = configured.getRight();
    }

    private OptimizerConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    public static final class Values {
        public final ModConfigSpec.BooleanValue changeAwareRefreshEnabled;
        public final ModConfigSpec.BooleanValue diagnosticsEnabled;
        public final ModConfigSpec.IntValue diagnosticsLogIntervalTicks;

        private Values(ModConfigSpec.Builder builder) {
            builder.push("decompressionPatterns");
            changeAwareRefreshEnabled = builder
                    .comment(
                            "Refresh AE2's global crafting provider only when MEGA Cells'",
                            "decompression pattern list changes. false restores stock per-tick refreshes.")
                    .define("changeAwareRefreshEnabled", true);
            builder.pop();

            builder.push("diagnostics");
            diagnosticsEnabled = builder
                    .comment("Periodically log per-grid scan and refresh counters.")
                    .define("diagnosticsEnabled", true);
            diagnosticsLogIntervalTicks = builder
                    .comment("Ticks between diagnostic log entries for each live service.")
                    .defineInRange("diagnosticsLogIntervalTicks", 1200, 20, 72000);
            builder.pop();
        }
    }
}
