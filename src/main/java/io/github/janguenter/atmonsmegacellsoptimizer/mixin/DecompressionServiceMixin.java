package io.github.janguenter.atmonsmegacellsoptimizer.mixin;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import gripe._90.megacells.misc.DecompressionService;
import io.github.janguenter.atmonsmegacellsoptimizer.config.OptimizerConfig;
import io.github.janguenter.atmonsmegacellsoptimizer.diagnostics.DiagnosticsRegistry;
import io.github.janguenter.atmonsmegacellsoptimizer.diagnostics.DiagnosticsReporter;
import io.github.janguenter.atmonsmegacellsoptimizer.diagnostics.ServiceMetrics;
import io.github.janguenter.atmonsmegacellsoptimizer.refresh.RefreshDecision;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DecompressionService.class)
public abstract class DecompressionServiceMixin {
    @Shadow
    @Final
    private List<IPatternDetails> patterns;

    @Shadow
    @Final
    private IGrid grid;

    @Shadow
    private int installedModules;

    @Unique
    private ServiceMetrics atmons$metrics;

    @Unique
    private List<IPatternDetails> atmons$previousPatterns = List.of();

    @Unique
    private boolean atmons$stockRefreshEncountered;

    @Unique
    private long atmons$tick;

    @Inject(
            method = "<init>(Lappeng/api/networking/IGrid;"
                    + "Lappeng/api/networking/crafting/ICraftingService;)V",
            at = @At("RETURN"),
            require = 1)
    private void atmons$initialize(
            IGrid constructorGrid,
            ICraftingService craftingService,
            CallbackInfo callback) {
        atmons$metrics = new ServiceMetrics();
        atmons$metrics.identify(constructorGrid.toString());
        DiagnosticsRegistry.register(this, atmons$metrics);
    }

    @Inject(method = "onServerEndTick()V", at = @At("HEAD"), require = 1)
    private void atmons$capturePreviousPatterns(CallbackInfo callback) {
        atmons$previousPatterns = List.copyOf(patterns);
        atmons$stockRefreshEncountered = false;
    }

    @Redirect(
            method = "onServerEndTick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/crafting/ICraftingService;"
                            + "refreshGlobalCraftingProvider("
                            + "Lappeng/api/networking/crafting/ICraftingProvider;)V"),
            require = 1)
    private void atmons$refreshWhenPatternsChanged(
            ICraftingService craftingService,
            ICraftingProvider provider) {
        atmons$stockRefreshEncountered = true;
        var enabled = OptimizerConfig.VALUES.changeAwareRefreshEnabled.get();
        if (RefreshDecision.shouldRefresh(
                enabled,
                true,
                atmons$previousPatterns,
                patterns)) {
            atmons$performRefresh(craftingService, provider, false, !enabled);
        } else {
            atmons$metrics.recordSkippedRefresh();
        }
    }

    @Inject(method = "onServerEndTick()V", at = @At("RETURN"), require = 1)
    private void atmons$finishPatternScan(CallbackInfo callback) {
        var enabled = OptimizerConfig.VALUES.changeAwareRefreshEnabled.get();
        if (!atmons$stockRefreshEncountered && RefreshDecision.shouldRefresh(
                enabled,
                false,
                atmons$previousPatterns,
                patterns)) {
            atmons$performRefresh(
                    grid.getCraftingService(),
                    (ICraftingProvider) (Object) this,
                    true,
                    false);
        } else if (!atmons$stockRefreshEncountered && enabled) {
            atmons$metrics.recordSkippedRefresh();
        }

        atmons$metrics.observeScan(installedModules, patterns.size());
        DiagnosticsReporter.maybeLog(atmons$metrics, atmons$tick++);
    }

    @Unique
    private void atmons$performRefresh(
            ICraftingService craftingService,
            ICraftingProvider provider,
            boolean moduleRemovalRefresh,
            boolean stockPassThrough) {
        var startedNanos = System.nanoTime();
        craftingService.refreshGlobalCraftingProvider(provider);
        atmons$metrics.recordRefresh(
                System.nanoTime() - startedNanos,
                moduleRemovalRefresh,
                stockPassThrough);
    }
}
