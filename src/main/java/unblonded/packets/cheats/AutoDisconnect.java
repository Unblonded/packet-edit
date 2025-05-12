package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import unblonded.packets.cfg;

public class AutoDisconnect {
    private static boolean primed = false;
    private static float proximity = 10;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!primed || mc.world == null || mc.player == null) return;

            if (PlayerTracker.closestPlayerDistance() < proximity) {
                mc.getNetworkHandler().getConnection().disconnect(Text.of("Player Proximity Check Triggered! Threshold: <" + proximity));
                primed = false;
                cfg.sendAutoDcFlag(false);
            }
        });
    }

    public static void setState(boolean state, float targetProx) {
        primed = state;
        proximity = targetProx;
    }
}
