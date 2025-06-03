package unblonded.packets.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import unblonded.packets.cfg;
import unblonded.packets.imgui.Alert;

public class AlertHandler {
    private static boolean wasOnFire = false;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && cfg.noRenderItems[0].get()) {
                boolean isOnFire = client.player.isOnFire();
                if (isOnFire && !wasOnFire)
                    Alert.info("Fire Alert", "You are on fire! Use water to extinguish yourself.");
                wasOnFire = isOnFire;
            }
        });
    }
}
