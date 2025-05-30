package unblonded.packets.cheats;

import imgui.type.ImInt;
import imgui.type.ImString;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.option.KeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class AutoSell {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private enum State {
        IDLE,
        SWITCHING_SLOT,
        SENDING_COMMAND,
        WAITING_FOR_GUI,
        CLICKING_CONFIRM,
        DELAY
    }

    private static State currentState = State.IDLE;
    private static int currentSlot = 0;
    private static long lastActionTime = 0;
    private static int DELAY_MS = 300; // Delay between actions in milliseconds
    private static boolean triggerActivated = false;  // Flag to detect state change
    private static boolean runningCycle = false;      // Flag to track if we're in the middle of a cycle
    private static String price = "0";// Default price
    private static int[] autoSellEndpoints = {0, 8};

    // Error handling variables
    private static int consecutiveErrors = 0;
    private static final int MAX_CONSECUTIVE_ERRORS = 2;
    private static int currentSlotRetries = 0;
    private static final int MAX_SLOT_RETRIES = 2;

    public static void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {return;}
            // Check if enabled status was just activated
            if (triggerActivated && !runningCycle) {
                runningCycle = true;
                currentState = State.SWITCHING_SLOT;
                currentSlot = autoSellEndpoints[0];
                consecutiveErrors = 0; // Reset error counter when starting new cycle
                currentSlotRetries = 0;
                client.player.sendMessage(Text.of("AutoSell cycle starting"), false);
                if (client.currentScreen != null) client.currentScreen.close();
                triggerActivated = false;
            }

            if (client.player == null || client.world == null || currentState == State.IDLE) return;

            long currentTime = System.currentTimeMillis();

            // Check if we need to wait
            if (currentState == State.DELAY) {
                if (currentTime - lastActionTime >= DELAY_MS) {
                    // Move to the next slot after delay
                    currentSlot++;
                    currentSlotRetries = 0; // Reset retries for new slot

                    // Check if we've gone through all slots
                    if (currentSlot > autoSellEndpoints[1]) {
                        stopCycle(client, "AutoSell cycle completed");
                    } else {
                        currentState = State.SWITCHING_SLOT;
                    }
                }
                return;
            }

            switch (currentState) {
                case SWITCHING_SLOT:
                    try {
                        client.player.getInventory().selectedSlot = currentSlot;

                        // Get the item in the current slot
                        if (!client.player.getInventory().getStack(currentSlot).isEmpty()) {
                            client.player.sendMessage(Text.of("Selected slot " + currentSlot), false);
                            currentState = State.SENDING_COMMAND;
                            consecutiveErrors = 0;
                        } else {
                            client.player.sendMessage(Text.of("Skipping empty slot " + currentSlot), false);
                            currentState = State.DELAY;
                        }
                        lastActionTime = currentTime;
                    } catch (Exception e) {
                        handleError(client, "Error switching to slot " + currentSlot + ": " + e.getMessage());
                    }
                    break;

                case SENDING_COMMAND:
                    try {
                        // Send the sell command after switching slots
                        String command = "/ah sell " + price;
                        client.player.networkHandler.sendCommand(command.substring(1));
                        client.player.sendMessage(Text.of("Sending sell command for slot " + currentSlot + " with price " + price), false);

                        currentState = State.WAITING_FOR_GUI;
                        lastActionTime = currentTime;
                        consecutiveErrors = 0; // Reset on success
                    } catch (Exception e) {
                        handleError(client, "Error sending command for slot " + currentSlot + ": " + e.getMessage());
                    }
                    break;

                case WAITING_FOR_GUI:
                    try {
                        // Check if the GUI is open
                        if (client.currentScreen instanceof HandledScreen) {
                            currentState = State.CLICKING_CONFIRM;
                            consecutiveErrors = 0; // Reset on success
                        } else {
                            // If GUI hasn't opened yet, wait a bit longer
                            if (currentTime - lastActionTime > 3000) { // 3 second timeout
                                handleError(client, "Timeout waiting for GUI on slot " + currentSlot);
                            }
                        }
                    } catch (Exception e) {
                        handleError(client, "Error waiting for GUI on slot " + currentSlot + ": " + e.getMessage());
                    }
                    break;

                case CLICKING_CONFIRM:
                    try {
                        if (client.currentScreen instanceof HandledScreen) {
                            HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
                            int confirmButtonSlot = findConfirmButtonSlot(screen);

                            if (confirmButtonSlot != -1) {
                                // Click the confirm button
                                client.interactionManager.clickSlot(
                                        screen.getScreenHandler().syncId,
                                        confirmButtonSlot,
                                        0,
                                        SlotActionType.PICKUP,
                                        client.player
                                );

                                // Close the GUI
                                client.player.closeHandledScreen();

                                currentState = State.DELAY;
                                lastActionTime = currentTime;
                                consecutiveErrors = 0; // Reset on success
                            } else {
                                handleError(client, "Could not find confirm button for slot " + currentSlot);
                            }
                        } else {
                            // GUI was closed unexpectedly
                            handleError(client, "GUI closed unexpectedly for slot " + currentSlot);
                        }
                    } catch (Exception e) {
                        handleError(client, "Error clicking confirm for slot " + currentSlot + ": " + e.getMessage());
                    }
                    break;
            }
        });
    }

    private static void handleError(MinecraftClient client, String errorMessage) {
        consecutiveErrors++;
        currentSlotRetries++;

        client.player.sendMessage(Text.of("§c" + errorMessage + " (Error " + consecutiveErrors + "/" + MAX_CONSECUTIVE_ERRORS + ")"), false);

        // Check if we should stop due to too many consecutive errors
        if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
            stopCycle(client, "§cAutoSell stopped: Too many consecutive errors (" + consecutiveErrors + ")");
            return;
        }

        // Check if we should skip this slot due to too many retries
        if (currentSlotRetries >= MAX_SLOT_RETRIES) {
            client.player.sendMessage(Text.of("§eSkipping slot " + currentSlot + " after " + MAX_SLOT_RETRIES + " failures"), false);
            currentState = State.DELAY;
            lastActionTime = System.currentTimeMillis();
            currentSlotRetries = 0;
        } else {
            // Retry the current slot after a delay
            currentState = State.DELAY;
            lastActionTime = System.currentTimeMillis();
        }
    }

    private static void stopCycle(MinecraftClient client, String message) {
        currentState = State.IDLE;
        runningCycle = false;
        consecutiveErrors = 0;
        currentSlotRetries = 0;

        // Close any open screens
        if (client.currentScreen != null) {
            client.currentScreen.close();
        }

        client.player.sendMessage(Text.of(message), false);
    }

    private static int findConfirmButtonSlot(HandledScreen<?> screen) {
        try {
            ScreenHandler handler = screen.getScreenHandler();
            for (int i = 0; i < handler.slots.size(); i++) {
                Slot slot = handler.slots.get(i);
                ItemStack stack = slot.getStack();

                if (stack != null && !stack.isEmpty())
                    if (stack.getItem() == Items.LIME_STAINED_GLASS_PANE)
                        return i;
            }
        } catch (Exception e) {
            // Return -1 if there's an error finding the slot
        }

        return -1;
    }

    public static void setState(boolean state, int delay, ImString targetPrice, ImInt[] endpoints) {
        if (state) {
            triggerActivated = true;
            DELAY_MS = delay;
            price = targetPrice.get();
            autoSellEndpoints = new int[]{endpoints[0].get()-1, endpoints[1].get()-1};
        } else {
            // If manually disabled, stop the cycle
            if (runningCycle) {
                stopCycle(mc, "AutoSell manually stopped");
            }
        }
    }

    // Utility method to check if AutoSell is currently running
    public static boolean isRunning() {
        return runningCycle;
    }

    // Method to manually stop the cycle (can be called from UI)
    public static void forceStop() {
        if (runningCycle) {
            stopCycle(mc, "AutoSell force stopped");
        }
    }
}