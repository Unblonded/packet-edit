package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import unblonded.packets.cfg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoDisconnect {
    private static boolean primed = false;
    public static boolean dePrime = false;
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
