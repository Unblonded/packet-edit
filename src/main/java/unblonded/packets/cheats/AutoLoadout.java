package unblonded.packets.cheats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import unblonded.packets.cfg;
import unblonded.packets.util.KitSlot;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoLoadout {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void saveCurrentLoadout(String name) {
        if (mc.player == null) {
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            mc.player.sendMessage(Text.literal("§cLoadout name cannot be empty!"), false);
            return;
        }

        PlayerInventory inv = mc.player.getInventory();
        List<KitSlot> kit = new ArrayList<>();

        // Save all non-empty slots
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                kit.add(new KitSlot(i, stack.getItem()));
            }
        }

        if (kit.isEmpty()) {
            mc.player.sendMessage(Text.literal("§cCannot save empty loadout!"), false);
            return;
        }

        cfg.savedLoadouts.put(name.trim(), kit);
        mc.player.sendMessage(Text.literal("§aSaved loadout: " + name.trim() + " (" + kit.size() + " items)"), false);
    }

    public static void applyLoadout(String name) {
        applyLoadout(name, 100); // Default 100ms delay between moves
    }

    public static void applyLoadout(String name, int delayMs) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }

        if (name == null) {
            mc.player.sendMessage(Text.literal("§cNo loadout selected!"), false);
            return;
        }

        PlayerInventory inv = mc.player.getInventory();
        List<KitSlot> kit = cfg.savedLoadouts.get(name);

        if (kit == null) {
            mc.player.sendMessage(Text.literal("§cLoadout '" + name + "' not found!"), false);
            return;
        }

        Set<Integer> processedSlots = new HashSet<>();
        List<SwapAction> swapQueue = new ArrayList<>();
        int skippedItems = 0;

        // Build list of swaps needed
        for (KitSlot target : kit) {
            // Skip if slot is out of bounds
            if (target.slotIndex >= inv.size() || target.slotIndex < 0) {
                continue;
            }

            ItemStack current = inv.getStack(target.slotIndex);

            // If correct item is already in position, skip and mark as processed
            if (!current.isEmpty() && current.getItem().equals(target.item)) {
                processedSlots.add(target.slotIndex);
                skippedItems++;
                continue;
            }

            // Find the item in inventory
            for (int i = 0; i < inv.size(); i++) {
                if (processedSlots.contains(i) || i == target.slotIndex) {
                    continue;
                }

                ItemStack candidate = inv.getStack(i);
                if (!candidate.isEmpty() && candidate.getItem().equals(target.item)) {
                    // Queue swap action
                    swapQueue.add(new SwapAction(i, target.slotIndex));
                    processedSlots.add(i);
                    processedSlots.add(target.slotIndex);
                    break;
                }
            }
        }

        if (swapQueue.isEmpty()) {
            String message = "§bLoadout '" + name + "' already applied!";
            if (skippedItems > 0) {
                message += " (" + skippedItems + " items already in place)";
            }
            mc.player.sendMessage(Text.literal(message), false);
            return;
        }

        // Execute swaps with delay
        executeSwapsWithDelay(swapQueue, name, delayMs, skippedItems);
    }

    private static void executeSwapsWithDelay(List<SwapAction> swapQueue, String loadoutName, int delayMs, int skippedItems) {
        if (swapQueue.isEmpty()) {
            return;
        }

        AtomicInteger currentIndex = new AtomicInteger(0);
        int totalSwaps = swapQueue.size();

        // Send initial message
        mc.player.sendMessage(Text.literal("§bApplying loadout: " + loadoutName + " (" + totalSwaps + " moves)..."), false);

        Runnable swapTask = new Runnable() {
            @Override
            public void run() {
                try {
                    // Check if player is still valid
                    if (mc.player == null || mc.interactionManager == null) {
                        if (mc.player != null) {
                            mc.player.sendMessage(Text.literal("§cLoadout application cancelled - player disconnected"), false);
                        }
                        return;
                    }

                    int index = currentIndex.get();
                    if (index >= swapQueue.size()) {
                        // All swaps completed
                        String message = "§aLoadout '" + loadoutName + "' applied! (" + totalSwaps + " moved";
                        if (skippedItems > 0) {
                            message += ", " + skippedItems + " already in place";
                        }
                        message += ")";
                        mc.player.sendMessage(Text.literal(message), false);
                        return;
                    }

                    SwapAction swap = swapQueue.get(index);

                    // Perform the swap on the main thread
                    mc.execute(() -> {
                        try {
                            swapSlots(swap.fromSlot, swap.toSlot);
                        } catch (Exception e) {
                            System.err.println("Error during slot swap: " + e.getMessage());
                        }
                    });

                    currentIndex.incrementAndGet();

                    // Schedule next swap if there are more
                    if (currentIndex.get() < swapQueue.size()) {
                        scheduler.schedule(this, delayMs, TimeUnit.MILLISECONDS);
                    }
                } catch (Exception e) {
                    System.err.println("Error in loadout application: " + e.getMessage());
                    mc.execute(() -> {
                        if (mc.player != null) {
                            mc.player.sendMessage(Text.literal("§cError applying loadout: " + e.getMessage()), false);
                        }
                    });
                }
            }
        };

        // Start the first swap immediately
        scheduler.schedule(swapTask, 0, TimeUnit.MILLISECONDS);
    }

    public static void removeLoadout(String name) {
        if (name == null) {
            return;
        }

        if (cfg.savedLoadouts.remove(name) != null) {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§eDeleted loadout: " + name), false);
            }
        }
    }

    public static boolean hasLoadout(String name) {
        return name != null && cfg.savedLoadouts.containsKey(name);
    }

    public static Set<String> getLoadoutNames() {
        return new HashSet<>(cfg.savedLoadouts.keySet());
    }

    public static void clearAllLoadouts() {
        cfg.savedLoadouts.clear();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§eCleared all loadouts"), false);
        }
    }

    private static void swapSlots(int from, int to) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }

        try {
            // Convert inventory indices to screen handler slot indices
            int fromSlot = convertToScreenSlot(from);
            int toSlot = convertToScreenSlot(to);

            // Pick up item from source slot
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    fromSlot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );

            // Place/swap with destination slot
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    toSlot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );

            // If there was an item in destination, place it back in source
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    fromSlot,
                    0,
                    SlotActionType.PICKUP,
                    mc.player
            );
        } catch (Exception e) {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("§cError moving items: " + e.getMessage()), false);
            }
        }
    }

    private static int convertToScreenSlot(int inventorySlot) {
        // Hotbar slots (0-8 in inventory)
        if (inventorySlot >= 0 && inventorySlot <= 8) {
            return inventorySlot + 36; // Convert to screen handler slots 36-44
        }
        // Main inventory slots (9-35 in inventory) stay the same
        else if (inventorySlot >= 9 && inventorySlot <= 35) {
            return inventorySlot;
        }
        else if (inventorySlot == 36) { // Boots
            return 8;
        }
        else if (inventorySlot == 37) { // Leggings
            return 7;
        }
        else if (inventorySlot == 38) { // Chestplate
            return 6;
        }
        else if (inventorySlot == 39) { // Helmet
            return 5;
        }
        // Offhand slot (40 in inventory) becomes slot 45 in screen handler
        else if (inventorySlot == 40) {
            return 45;
        }
        // For any other slots, return as-is (shouldn't happen in normal inventory)
        return inventorySlot;
    }

    // Helper class to store swap actions
    private static class SwapAction {
        final int fromSlot;
        final int toSlot;

        SwapAction(int fromSlot, int toSlot) {
            this.fromSlot = fromSlot;
            this.toSlot = toSlot;
        }
    }

    // Call this when your mod shuts down to clean up the scheduler
    public static void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}