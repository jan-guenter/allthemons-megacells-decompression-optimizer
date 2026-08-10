package io.github.janguenter.atmonsmegacellsoptimizer.command;

import io.github.janguenter.atmonsmegacellsoptimizer.config.OptimizerConfig;
import io.github.janguenter.atmonsmegacellsoptimizer.diagnostics.DiagnosticsRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class DiagnosticsCommand {
    private DiagnosticsCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("atmonsmegacells")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> showStatus(context.getSource()))
                        .then(Commands.literal("status")
                                .executes(context -> showStatus(context.getSource())))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(4))
                                .executes(context -> reset(context.getSource()))));
    }

    private static int showStatus(CommandSourceStack source) {
        var snapshots = DiagnosticsRegistry.snapshots();
        source.sendSuccess(
                () -> Component.literal(
                        "MEGA Cells optimizer: " + snapshots.size()
                                + " live service(s), change-aware="
                                + OptimizerConfig.VALUES.changeAwareRefreshEnabled.get()),
                false);
        for (var snapshot : snapshots) {
            source.sendSuccess(() -> Component.literal(snapshot.diagnosticLine()), false);
        }
        return snapshots.size();
    }

    private static int reset(CommandSourceStack source) {
        var resetCount = DiagnosticsRegistry.resetCounters();
        source.sendSuccess(
                () -> Component.literal(
                        "Reset MEGA Cells optimizer counters for " + resetCount + " live service(s)"),
                true);
        return resetCount;
    }
}
