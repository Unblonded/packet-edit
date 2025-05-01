package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerTracker implements ClientModInitializer {
    private static final ArrayList<String> nearbyPlayers = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                nearbyPlayers.clear();
                double range = 20.0; // detection radius
                for (Player player : client.level.players()) {
                    if (!player.getName().getString().equals(client.player.getName().getString()) &&
                            player.distanceToSqr(client.player) <= range * range) {
                        double distance = Math.sqrt(player.distanceToSqr(client.player));
                        nearbyPlayers.add(player.getName().getString() + " - " + String.format("%.1f", distance) + "m");
                    }
                }
            }
        });
    }

    public static List<String> getNearbyPlayers() {
        return new ArrayList<>(nearbyPlayers);
    }
}
