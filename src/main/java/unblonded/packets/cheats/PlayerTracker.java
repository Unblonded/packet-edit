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
    private static final ArrayList<PlayerInfo> nearbyPlayers = new ArrayList<>();
    private static final Map<String, String> enchantmentAbbreviations = new HashMap<>();

    public static class PlayerInfo {
        public final String name;
        public final float distance;
        public final String[] armor;
        public final String mainhand;
        public final String offhand;
        public final float health;
        public final int armorTuffness;
        public final boolean isSneaking;
        public final boolean isSprinting;

        public PlayerInfo(String name, float distance, String[] armor, String mainhand, String offhand, float health, int armorTuffness, boolean isSneaking, boolean isSprinting) {
            this.name = name;
            this.distance = distance;
            this.armor = armor;
            this.mainhand = mainhand;
            this.offhand = offhand;
            this.health = health;
            this.armorTuffness = armorTuffness;
            this.isSneaking = isSneaking;
            this.isSprinting = isSprinting;
        }
    }

    static {
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

                        String[] armor = new String[4];
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                                ItemStack stack = player.getEquippedStack(slot);
                                armor[slot.getEntitySlotId()] = formatItemWithEnchantments(stack);
                            }
                        }

                        armor = flipArray(armor);

                        ItemStack mainhandStack = player.getMainHandStack();
                        ItemStack offhandStack = player.getOffHandStack();

                        PlayerInfo info = new PlayerInfo(
                                player.getName().getString(),
                                (float) distance,
                                armor,
                                formatItemWithEnchantments(mainhandStack),
                                formatItemWithEnchantments(offhandStack),
                                player.getHealth(),
                                player.getArmor(),
                                player.isSneaking(),
                                player.isSprinting()
                        );


                        tempList.add(info);
                    }
                }

                tempList.sort(Comparator.comparingDouble(p -> p.distance));
                nearbyPlayers.addAll(tempList);
            }
        });
    }

    private static String formatItemWithEnchantments(ItemStack stack) {
        if (stack.isEmpty()) return "Empty";

        String itemName = stack.getName().getString();
        List<String> enchantments = getEnchantmentsFormatted(stack);

        if (enchantments.isEmpty()) return itemName;
        else return itemName + " - " + String.join(",", enchantments);
    }

    private static List<String> getEnchantmentsFormatted(ItemStack stack) {
        List<String> result = new ArrayList<>();
        if (stack.isEmpty()) return result;

        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments != null) {
            try {
                for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                    int level = enchantments.getLevel(enchantment);
                    Optional<Identifier> enchantIdOpt = enchantment.getKey().map(key -> key.getValue());

                    if (enchantIdOpt.isPresent()) {
                        Identifier enchantId = enchantIdOpt.get();
                        String key = enchantId.getPath();
                        String abbr = enchantmentAbbreviations.getOrDefault(key, key.substring(0, 1).toUpperCase());
                        result.add(abbr + level);
                    }
                }

                if (!result.isEmpty()) return result;
            } catch (Exception e) {
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

    private static String[] flipArray(String[] arr) {
        String[] flipped = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            flipped[i] = arr[arr.length - 1 - i];
        }
        return flipped;
    }
}