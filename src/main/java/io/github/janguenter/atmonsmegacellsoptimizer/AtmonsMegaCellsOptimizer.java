package io.github.janguenter.atmonsmegacellsoptimizer;

import com.mojang.logging.LogUtils;
import io.github.janguenter.atmonsmegacellsoptimizer.command.DiagnosticsCommand;
import io.github.janguenter.atmonsmegacellsoptimizer.config.OptimizerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value = AtmonsMegaCellsOptimizer.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class AtmonsMegaCellsOptimizer {
    public static final String MOD_ID = "atmons_megacells_optimizer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AtmonsMegaCellsOptimizer(IEventBus modEventBus, ModContainer modContainer) {
        OptimizerConfig.register(modContainer);
        CompatibilityGuard.verify();
        NeoForge.EVENT_BUS.addListener(DiagnosticsCommand::register);

        LOGGER.info(
                "All the Mons MEGA Cells Decompression Optimizer loaded: exact MEGA Cells 4.11.0 "
                        + "and AE2 19.2.17 guards passed");
    }
}
