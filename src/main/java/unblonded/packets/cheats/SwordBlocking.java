package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class SwordBlocking {
    public static boolean isEntityBlocking(ClientPlayerEntity player) {
        if (player == null) return false;
        return MinecraftClient.getInstance().options.useKey.isPressed()
                && player.getMainHandStack().getItem() instanceof SwordItem;
    }

    public static Hand getBlockingHand() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return Hand.MAIN_HAND;
        ItemStack main = player.getMainHandStack();
        return main.getItem() instanceof SwordItem ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }
}
