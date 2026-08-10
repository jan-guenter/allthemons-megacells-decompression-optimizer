package io.github.janguenter.atmonsmegacellsoptimizer;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import gripe._90.megacells.misc.DecompressionService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.neoforged.fml.ModList;

public final class CompatibilityGuard {
    private static final String EXPECTED_AE2 = "19.2.17";
    private static final String EXPECTED_MEGA_CELLS = "4.11.0";

    private CompatibilityGuard() {
    }

    public static void verify() {
        requireVersion("ae2", EXPECTED_AE2);
        requireVersion("megacells", EXPECTED_MEGA_CELLS);
        verifySignatures();
    }

    public static void verifySignatures() {
        try {
            requireConstructor(DecompressionService.class, IGrid.class, ICraftingService.class);
            requireMethod(DecompressionService.class, "onServerEndTick");
            requireMethod(DecompressionService.class, "getAvailablePatterns");
            requireMethod(
                    DecompressionService.class,
                    "setPatternPriority",
                    int.class,
                    IGridNode.class);
            requireField(DecompressionService.class, "patterns", List.class);
            requireField(DecompressionService.class, "grid", IGrid.class);
            requireField(DecompressionService.class, "installedModules", int.class);
            requireMethod(IPatternDetails.class, "getDefinition");
            requireMethod(
                    ICraftingService.class,
                    "refreshGlobalCraftingProvider",
                    appeng.api.networking.crafting.ICraftingProvider.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "MEGA Cells 4.11.0 decompression refresh signatures changed; refusing to start",
                    exception);
        }
    }

    private static void requireVersion(String modId, String expectedVersion) {
        var container = ModList.get().getModContainerById(modId);
        if (container.isEmpty()) {
            throw new IllegalStateException("Required mod " + modId + " is not loaded");
        }

        var actualVersion = container.get().getModInfo().getVersion().toString();
        if (!expectedVersion.equals(actualVersion)) {
            throw new IllegalStateException(
                    "Unsupported " + modId + " version " + actualVersion
                            + "; expected exactly " + expectedVersion);
        }
    }

    private static void requireConstructor(Class<?> owner, Class<?>... parameters)
            throws NoSuchMethodException {
        owner.getDeclaredConstructor(parameters);
    }

    private static Method requireMethod(Class<?> owner, String name, Class<?>... parameters)
            throws NoSuchMethodException {
        return owner.getDeclaredMethod(name, parameters);
    }

    private static Field requireField(Class<?> owner, String name, Class<?> type)
            throws NoSuchFieldException {
        var field = owner.getDeclaredField(name);
        if (field.getType() != type) {
            throw new NoSuchFieldException(
                    owner.getName() + '#' + name + " has type " + field.getType().getName()
                            + ", expected " + type.getName());
        }
        return field;
    }
}
