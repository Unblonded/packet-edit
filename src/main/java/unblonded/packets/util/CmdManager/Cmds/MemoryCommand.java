package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import unblonded.packets.util.CmdManager.Command;

public class MemoryCommand extends Command {
    public MemoryCommand() {
        super("memory", "Shows detailed client memory information");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Runtime runtime = Runtime.getRuntime();

            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            double usedPercent = (double) usedMemory / maxMemory * 100;

            if (client.player != null) {
                client.player.sendMessage(Text.literal("§6=== Client Memory Information ==="), false);
                client.player.sendMessage(Text.literal(String.format("§7Used: %.1f MB (%.1f%%)",
                        usedMemory / 1024.0 / 1024.0, usedPercent)), false);
                client.player.sendMessage(Text.literal(String.format("§7Allocated: %.1f MB",
                        totalMemory / 1024.0 / 1024.0)), false);
                client.player.sendMessage(Text.literal(String.format("§7Maximum: %.1f MB",
                        maxMemory / 1024.0 / 1024.0)), false);
                client.player.sendMessage(Text.literal(String.format("§7Free in allocated: %.1f MB",
                        freeMemory / 1024.0 / 1024.0)), false);
            }

            return 1;
        });
    }
}