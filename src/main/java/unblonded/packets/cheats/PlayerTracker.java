package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class PlayerTracker implements ClientModInitializer {
    private static final ArrayList<String> nearbyPlayers = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                nearbyPlayers.clear();
                double range = 256;

                List<PlayerDistance> tempList = new ArrayList<>();

                for (Player player : client.level.players()) {
                    if (!player.getName().getString().equals(client.player.getName().getString()) &&
                            player.distanceToSqr(client.player) <= range * range) {
                        double distance = Math.sqrt(player.distanceToSqr(client.player));
                        tempList.add(new PlayerDistance(player.getName().getString(), distance));
                    }
                }

                // Sort by distance
                tempList.sort(Comparator.comparingDouble(p -> p.distance));

                // Format and add to the final list
                for (PlayerDistance pd : tempList) {
                    nearbyPlayers.add(pd.name + " - " + String.format("%.1f", pd.distance) + "m");
                }
            }
        });
    }

    public static List<String> getNearbyPlayers() {
        return new ArrayList<>(nearbyPlayers);
    }

    private static class PlayerDistance {
        String name;
        double distance;

        PlayerDistance(String name, double distance) {
            this.name = name;
            this.distance = distance;
        }
    }
}
