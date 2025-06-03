package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import unblonded.packets.cfg;

public class TimeChanger {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (mc.world == null || !cfg.timeChanger.get()) return;
            mc.world.getLevelProperties().setTimeOfDay(cfg.timeChangerLTime[0]);
        });
    }
}
