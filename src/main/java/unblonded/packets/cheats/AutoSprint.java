package unblonded.packets.cheats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import unblonded.packets.cfg;

public class AutoSprint {
    public static void run() {
        if (cfg.autosprint) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;
            if(player.horizontalCollision || player.isSneaking()) return;
            if(player.isInFluid() || player.isInLava()) return;
            if(player.getMovementSpeed() <= 1e-5F) return;

            if (!player.isSprinting()) {
                player.setSprinting(true);
            }
        }
    }
}
