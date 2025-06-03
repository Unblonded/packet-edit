package unblonded.packets.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Chat {
    public static String prefix = "§6§l>§bPacket Edit§6§l<§7:§r";

    public static void sendMessage(String message) {
        if (message == null || message.isEmpty()) return;
        MinecraftClient.getInstance().player.sendMessage(Text.literal(message), false);
    }
}
