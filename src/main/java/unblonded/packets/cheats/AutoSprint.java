package unblonded.packets.cheats;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import unblonded.packets.cfg;

public class AutoSprint {
    public static void run() {
        if (cfg.autosprint) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            if(player.horizontalCollision || player.isCrouching()) return;
            if(player.isInWater() || player.isUnderWater()) return;
            if(player.getDeltaMovement().length() <= 1e-5F) return;

            if (!player.isSprinting()) {
                player.setSprinting(true);
            }
        }
    }
}
