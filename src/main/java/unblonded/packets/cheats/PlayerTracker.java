package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class PlayerTracker {
    private static final ArrayList<String> nearbyPlayers = new ArrayList<>();

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                nearbyPlayers.clear();
                double range = 256;

                List<PlayerDistance> tempList = new ArrayList<>();

                for (PlayerEntity player : client.world.getPlayers()) {
                    if (!player.getName().getString().equals(client.player.getName().getString()) &&
                            player.squaredDistanceTo(client.player) <= range * range) {
                        double distance = Math.sqrt(player.squaredDistanceTo(client.player));
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

    public static float closestPlayerDistance() {
        if (nearbyPlayers.isEmpty()) return Float.MAX_VALUE;
        String closestPlayer = nearbyPlayers.get(0);
        String[] parts = closestPlayer.split(" - ");
        return Float.parseFloat(parts[1].replace("m", ""));
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
