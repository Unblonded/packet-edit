package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.enchantment.Enchantment;

import java.util.*;

public class PlayerTracker {
    // Change nearbyPlayers to hold PlayerInfo instead of just strings
    private static final ArrayList<PlayerInfo> nearbyPlayers = new ArrayList<>();
    // Map to store enchantment abbreviations
    private static final Map<String, String> enchantmentAbbreviations = new HashMap<>();

    static {
        // Initialize enchantment abbreviations
        enchantmentAbbreviations.put("enchantment.minecraft.protection", "P");
        enchantmentAbbreviations.put("enchantment.minecraft.fire_protection", "FP");
        enchantmentAbbreviations.put("enchantment.minecraft.feather_falling", "FF");
        enchantmentAbbreviations.put("enchantment.minecraft.blast_protection", "BP");
        enchantmentAbbreviations.put("enchantment.minecraft.projectile_protection", "PP");
        enchantmentAbbreviations.put("enchantment.minecraft.respiration", "R");
        enchantmentAbbreviations.put("enchantment.minecraft.aqua_affinity", "A");
        enchantmentAbbreviations.put("enchantment.minecraft.thorns", "T");
        enchantmentAbbreviations.put("enchantment.minecraft.depth_strider", "DS");
        enchantmentAbbreviations.put("enchantment.minecraft.frost_walker", "FW");
        enchantmentAbbreviations.put("enchantment.minecraft.binding_curse", "CB");
        enchantmentAbbreviations.put("enchantment.minecraft.sharpness", "S");
        enchantmentAbbreviations.put("enchantment.minecraft.smite", "SM");
        enchantmentAbbreviations.put("enchantment.minecraft.bane_of_arthropods", "BoA");
        enchantmentAbbreviations.put("enchantment.minecraft.knockback", "K");
        enchantmentAbbreviations.put("enchantment.minecraft.fire_aspect", "FA");
        enchantmentAbbreviations.put("enchantment.minecraft.looting", "L");
        enchantmentAbbreviations.put("enchantment.minecraft.sweeping", "SW");
        enchantmentAbbreviations.put("enchantment.minecraft.efficiency", "E");
        enchantmentAbbreviations.put("enchantment.minecraft.silk_touch", "ST");
        enchantmentAbbreviations.put("enchantment.minecraft.unbreaking", "U");
        enchantmentAbbreviations.put("enchantment.minecraft.fortune", "F");
        enchantmentAbbreviations.put("enchantment.minecraft.power", "P");
        enchantmentAbbreviations.put("enchantment.minecraft.punch", "PU");
        enchantmentAbbreviations.put("enchantment.minecraft.flame", "FL");
        enchantmentAbbreviations.put("enchantment.minecraft.infinity", "I");
        enchantmentAbbreviations.put("enchantment.minecraft.luck_of_the_sea", "LoS");
        enchantmentAbbreviations.put("enchantment.minecraft.lure", "LU");
        enchantmentAbbreviations.put("enchantment.minecraft.loyalty", "LO");
        enchantmentAbbreviations.put("enchantment.minecraft.impaling", "IM");
        enchantmentAbbreviations.put("enchantment.minecraft.riptide", "R");
        enchantmentAbbreviations.put("enchantment.minecraft.channeling", "CH");
        enchantmentAbbreviations.put("enchantment.minecraft.multishot", "M");
        enchantmentAbbreviations.put("enchantment.minecraft.piercing", "PI");
        enchantmentAbbreviations.put("enchantment.minecraft.quick_charge", "QC");
        enchantmentAbbreviations.put("enchantment.minecraft.mending", "M");
        enchantmentAbbreviations.put("enchantment.minecraft.vanishing_curse", "CV");
        enchantmentAbbreviations.put("enchantment.minecraft.soul_speed", "SS");
        enchantmentAbbreviations.put("enchantment.minecraft.swift_sneak", "SS");
        enchantmentAbbreviations.put("protection", "P");
        enchantmentAbbreviations.put("fire_protection", "FP");
        enchantmentAbbreviations.put("feather_falling", "FF");
        enchantmentAbbreviations.put("blast_protection", "BP");
        enchantmentAbbreviations.put("projectile_protection", "PP");
        enchantmentAbbreviations.put("respiration", "R");
        enchantmentAbbreviations.put("aqua_affinity", "A");
        enchantmentAbbreviations.put("thorns", "T");
        enchantmentAbbreviations.put("depth_strider", "DS");
        enchantmentAbbreviations.put("frost_walker", "FW");
        enchantmentAbbreviations.put("binding_curse", "CB");
        enchantmentAbbreviations.put("sharpness", "S");
        enchantmentAbbreviations.put("smite", "SM");
        enchantmentAbbreviations.put("bane_of_arthropods", "BoA");
        enchantmentAbbreviations.put("knockback", "K");
        enchantmentAbbreviations.put("fire_aspect", "FA");
        enchantmentAbbreviations.put("looting", "L");
        enchantmentAbbreviations.put("sweeping", "SW");
        enchantmentAbbreviations.put("efficiency", "E");
        enchantmentAbbreviations.put("silk_touch", "ST");
        enchantmentAbbreviations.put("unbreaking", "U");
        enchantmentAbbreviations.put("fortune", "F");
        enchantmentAbbreviations.put("power", "P");
        enchantmentAbbreviations.put("punch", "PU");
        enchantmentAbbreviations.put("flame", "FL");
        enchantmentAbbreviations.put("infinity", "I");
        enchantmentAbbreviations.put("luck_of_the_sea", "LoS");
        enchantmentAbbreviations.put("lure", "LU");
        enchantmentAbbreviations.put("loyalty", "LO");
        enchantmentAbbreviations.put("impaling", "IM");
        enchantmentAbbreviations.put("riptide", "R");
        enchantmentAbbreviations.put("channeling", "CH");
        enchantmentAbbreviations.put("multishot", "M");
        enchantmentAbbreviations.put("piercing", "PI");
        enchantmentAbbreviations.put("quick_charge", "QC");
        enchantmentAbbreviations.put("mending", "M");
        enchantmentAbbreviations.put("vanishing_curse", "CV");
        enchantmentAbbreviations.put("soul_speed", "SS");
        enchantmentAbbreviations.put("swift_sneak", "SS");
    }

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                nearbyPlayers.clear();
                double range = 256;

                List<PlayerInfo> tempList = new ArrayList<>();

                for (PlayerEntity player : client.world.getPlayers()) {
                    if (!player.getName().getString().equals(client.player.getName().getString()) &&
                            player.squaredDistanceTo(client.player) <= range * range) {

                        double distance = Math.sqrt(player.squaredDistanceTo(client.player));

                        // Collect armor strings
                        String[] armor = new String[4];
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                                ItemStack stack = player.getEquippedStack(slot);
                                armor[slot.getEntitySlotId()] = formatItemWithEnchantments(stack);
                            }
                        }

                        armor = flipArray(armor);

                        // Mainhand and Offhand
                        ItemStack mainhandStack = player.getMainHandStack();
                        ItemStack offhandStack = player.getOffHandStack();

                        PlayerInfo info = new PlayerInfo(
                                player.getName().getString(),
                                (float) distance,
                                armor,
                                formatItemWithEnchantments(mainhandStack),
                                formatItemWithEnchantments(offhandStack)
                        );

                        tempList.add(info);
                    }
                }

                // Sort by distance
                tempList.sort(Comparator.comparingDouble(p -> p.distance));

                nearbyPlayers.addAll(tempList);
            }
        });
    }

    // Format an item with its enchantments in the desired format
    private static String formatItemWithEnchantments(ItemStack stack) {
        if (stack.isEmpty()) return "Empty";

        String itemName = stack.getName().getString();
        List<String> enchantments = getEnchantmentsFormatted(stack);

        if (enchantments.isEmpty()) {
            return itemName;
        } else {
            return itemName + " - " + String.join(",", enchantments);
        }
    }

    // Extract enchantments from an item and format them
    private static List<String> getEnchantmentsFormatted(ItemStack stack) {
        List<String> result = new ArrayList<>();

        if (stack.isEmpty()) return result;

        // Try the component API first
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments != null) {
            try {
                // Iterate through all enchantments on the item
                for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                    int level = enchantments.getLevel(enchantment);

                    // Get the identifier for this enchantment
                    Optional<Identifier> enchantIdOpt = enchantment.getKey().map(key -> key.getValue());

                    if (enchantIdOpt.isPresent()) {
                        Identifier enchantId = enchantIdOpt.get();
                        String key = enchantId.getPath(); // e.g. "protection"
                        String abbr = enchantmentAbbreviations.getOrDefault(
                                key, key.substring(0, 1).toUpperCase()
                        );
                        result.add(abbr + level); // e.g. "P5"
                    }
                }

                // If we got results, return them
                if (!result.isEmpty()) {
                    return result;
                }
            } catch (Exception e) {
                // If component API fails, continue to NBT fallback
                System.out.println("Component API failed: " + e.getMessage());
            }
        }


        return result;
    }




    public static List<PlayerInfo> getNearbyPlayers() {
        return new ArrayList<>(nearbyPlayers);
    }

    public static float closestPlayerDistance() {
        if (nearbyPlayers.isEmpty()) return Float.MAX_VALUE;
        return nearbyPlayers.get(0).distance;
    }

    // New data class to hold detailed player info
    public static class PlayerInfo {
        public final String name;
        public final float distance;
        public final String[] armor; // 4 pieces
        public final String mainhand;
        public final String offhand;

        public PlayerInfo(String name, float distance, String[] armor, String mainhand, String offhand) {
            this.name = name;
            this.distance = distance;
            this.armor = armor;
            this.mainhand = mainhand;
            this.offhand = offhand;
        }
    }

    private static String[] flipArray(String[] arr) {
        String[] flipped = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            flipped[i] = arr[arr.length - 1 - i];
        }
        return flipped;
    }
}