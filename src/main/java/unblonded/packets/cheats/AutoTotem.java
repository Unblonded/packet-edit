package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.util.util;

import java.util.Timer;
import java.util.TimerTask;

public class AutoTotem implements ClientModInitializer {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private KeyBinding toggleBinding;

    // Simple boolean toggle
    private static boolean enabled = true;

    // Track totem state
    private boolean hadTotemLastTick = false;
    private boolean intentionalSwitch = false;
    private boolean justSwitchedItem = false;

    // Timer for reequipping totems
    private Timer reequipTimer;
    private boolean reequipScheduled = false;

    // Delay in milliseconds (500ms = 0.5 seconds by default)
    private static long reequipDelayMs = 500;

    // Last health tracking for more accurate totem pop detection
    private float lastHealth = 0;

    @Override
    public void onInitializeClient() {
        // Register keybinding for toggling
        toggleBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autototem.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.autototem"
        ));

        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check for toggle key press
            if (toggleBinding.wasPressed()) {
                enabled = !enabled;

                if (MC.player != null) {
                    MC.player.sendMessage(Text.literal("Auto Totem: " +
                            (enabled ? "§aEnabled" : "§cDisabled")), true);
                }
            }

            // Handle player interaction with inventory
            if (MC.player != null) {
                if (MC.options.useKey.wasPressed() || MC.options.attackKey.wasPressed() ||
                        MC.options.pickItemKey.wasPressed() || MC.options.swapHandsKey.wasPressed()) {
                    intentionalSwitch = true;
                }
            }

            // Execute auto totem functionality
            tickAutoTotem();
        });
    }

    private void tickAutoTotem() {
        // Check if feature is enabled and player exists
        if (!enabled ||
                MC.player == null ||
                MC.world == null ||
                MC.player.isCreative() ||
                MC.player.isSpectator()) {
            return;
        }

        // Reset justSwitchedItem flag after one tick
        if (justSwitchedItem) {
            justSwitchedItem = false;
        }

        // Check if player has a totem in offhand
        ItemStack offhandStack = MC.player.getStackInHand(Hand.OFF_HAND);
        boolean hasTotemNow = offhandStack.isOf(Items.TOTEM_OF_UNDYING);
        float currentHealth = MC.player.getHealth();

        // If player had a totem last tick but doesn't have one now
        if (hadTotemLastTick && !hasTotemNow) {
            // Check if health dropped significantly or player was in near-death state
            // This helps detect actual totem pops vs. manual switches
            boolean likelyTotemPop = lastHealth <= 2.0f ||
                    currentHealth < lastHealth - 6.0f ||
                    MC.player.hurtTime > 0;

            if (likelyTotemPop && !intentionalSwitch) {
                // Schedule the reequip task with the timer
                if (!reequipScheduled) {
                    scheduleTotemReequip();
                }
            } else {
                // User likely switched items manually
                intentionalSwitch = true;
                justSwitchedItem = true;
            }
        }

        // Update the "had totem" tracking variable for next tick
        hadTotemLastTick = hasTotemNow;
        lastHealth = currentHealth;

        // Reset intentional switch flag after item changes
        if (intentionalSwitch && !justSwitchedItem) {
            intentionalSwitch = false;
        }
    }

    private void scheduleTotemReequip() {
        // Cancel any previous timer
        if (reequipTimer != null) {
            reequipTimer.cancel();
        }

        // Create a new timer
        reequipTimer = new Timer();
        reequipScheduled = true;

        if (MC.player != null) {
            MC.player.sendMessage(Text.literal("§eTotem popped! Will reequip in " + (reequipDelayMs / 1000.0) + " seconds..."), true);
        }

        // Schedule the reequip task
        reequipTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // This runs on a different thread, so we need to queue it to run on the main thread
                MC.execute(() -> {
                    tryReequipTotem();
                    reequipScheduled = false;
                });
            }
        }, reequipDelayMs);
    }

    private void tryReequipTotem() {
        if (MC.player == null) return;

        ItemStack offhandStack = MC.player.getStackInHand(Hand.OFF_HAND);

        // Only equip if offhand is empty
        if (offhandStack.isEmpty()) {
            int totemSlot = findTotemInInventory();
            if (totemSlot != -1) {
                moveTotemToOffhand(totemSlot);

                if (MC.player != null) {
                    MC.player.sendMessage(Text.literal("§aAuto Totem: Equipped new totem"), true);
                }
            } else {
                // No totems left
                if (MC.player != null) {
                    MC.player.sendMessage(Text.literal("§cAuto Totem: No totems found in inventory"), true);
                }
            }
        }
    }

    private int findTotemInInventory() {
        // Search player inventory for totems
        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                return i < 9 ? i + 36 : i; // Convert hotbar slots to container slots
            }
        }
        return -1; // No totem found
    }

    private void moveTotemToOffhand(int totemSlot) {
        // Make sure we're not in a container
        if (MC.interactionManager != null && MC.player.currentScreenHandler == MC.player.playerScreenHandler) {
            // Click on the totem slot
            MC.interactionManager.clickSlot(
                    MC.player.playerScreenHandler.syncId,
                    totemSlot,
                    0, // primary mouse button
                    SlotActionType.PICKUP, // pick up the item
                    MC.player
            );

            // Click on the offhand slot (45 is the offhand slot in the player container)
            MC.interactionManager.clickSlot(
                    MC.player.playerScreenHandler.syncId,
                    45,
                    0, // primary mouse button
                    SlotActionType.PICKUP, // place the item
                    MC.player
            );
        }
    }

    public static void setState(boolean state, int delayMs, int humanityMs) {
        enabled = state;
        if (humanityMs != 0) reequipDelayMs = delayMs + util.rndInt(humanityMs);
        else reequipDelayMs = delayMs;
    }
}