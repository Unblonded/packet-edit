package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import unblonded.packets.util.CmdManager.Command;

public class GCCommand extends Command {
    public GCCommand() {
        super("gc", "Forces garbage collection and shows memory freed");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            MinecraftClient client = MinecraftClient.getInstance();

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

            // Send results to chat
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§aGarbage collection executed!"), false);
                client.player.sendMessage(Text.literal(String.format("§7Before: %.1f MB used / %.1f MB total",
                        beforeUsed / 1024.0 / 1024.0, beforeTotal / 1024.0 / 1024.0)), false);
                client.player.sendMessage(Text.literal(String.format("§7After: %.1f MB used / %.1f MB total",
                        afterUsed / 1024.0 / 1024.0, afterTotal / 1024.0 / 1024.0)), false);
                client.player.sendMessage(Text.literal(String.format("§2Freed: %.1f MB",
                        freed / 1024.0 / 1024.0)), false);
            }

            return 1;
        });
    }
}