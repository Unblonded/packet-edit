package unblonded.packets.util;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class GCCommand {

    static public void onInitializeClient() {
        CommandRegistrationCallback.EVENT.register(GCCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("gc").executes(GCCommand::executeGC));
        dispatcher.register(CommandManager.literal("memory").executes(GCCommand::executeMemoryInfo));
    }

    private static int executeGC(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // Get memory info before GC
        Runtime runtime = Runtime.getRuntime();
        long beforeUsed = runtime.totalMemory() - runtime.freeMemory();
        long beforeTotal = runtime.totalMemory();

        // Force garbage collection
        System.gc();

        // Wait a moment for GC to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get memory info after GC
        long afterUsed = runtime.totalMemory() - runtime.freeMemory();
        long afterTotal = runtime.totalMemory();
        long freed = beforeUsed - afterUsed;

        // Send results to player/console
        source.sendFeedback(() -> Text.literal("§aGarbage collection executed!"), false);
        source.sendFeedback(() -> Text.literal(String.format("§7Before: %.1f MB used / %.1f MB total",
                beforeUsed / 1024.0 / 1024.0, beforeTotal / 1024.0 / 1024.0)), false);
        source.sendFeedback(() -> Text.literal(String.format("§7After: %.1f MB used / %.1f MB total",
                afterUsed / 1024.0 / 1024.0, afterTotal / 1024.0 / 1024.0)), false);
        source.sendFeedback(() -> Text.literal(String.format("§2Freed: %.1f MB",
                freed / 1024.0 / 1024.0)), false);

        return 1;
    }

    private static int executeMemoryInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double usedPercent = (double) usedMemory / maxMemory * 100;

        source.sendFeedback(() -> Text.literal("§6=== Memory Information ==="), false);
        source.sendFeedback(() -> Text.literal(String.format("§7Used: %.1f MB (%.1f%%)",
                usedMemory / 1024.0 / 1024.0, usedPercent)), false);
        source.sendFeedback(() -> Text.literal(String.format("§7Allocated: %.1f MB",
                totalMemory / 1024.0 / 1024.0)), false);
        source.sendFeedback(() -> Text.literal(String.format("§7Maximum: %.1f MB",
                maxMemory / 1024.0 / 1024.0)), false);
        source.sendFeedback(() -> Text.literal(String.format("§7Free in allocated: %.1f MB",
                freeMemory / 1024.0 / 1024.0)), false);

        return 1;
    }
}