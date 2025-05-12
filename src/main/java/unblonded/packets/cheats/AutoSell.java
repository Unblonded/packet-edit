package unblonded.packets.cheats;

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

    public static void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if enabled status was just activated
            if (triggerActivated && !runningCycle) {
                runningCycle = true;
                currentState = State.SWITCHING_SLOT;
                currentSlot = autoSellEndpoints[0];
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

                    // Check if we've gone through all slots
                    if (currentSlot > autoSellEndpoints[1]) {
                        currentState = State.IDLE;
                        runningCycle = false;  // Mark cycle as complete
                        client.player.sendMessage(Text.of("AutoSell cycle completed"), false);
                    } else {
                        currentState = State.SWITCHING_SLOT;
                    }
                }
                return;
            }

            switch (currentState) {
                case SWITCHING_SLOT:
                    // Switch to the current slot
                    client.player.getInventory().selectedSlot = currentSlot;

                    // Get the item in the current slot
                    if (!client.player.getInventory().getStack(currentSlot).isEmpty()) {
                        client.player.sendMessage(Text.of("Selected slot " + currentSlot), false);
                        currentState = State.SENDING_COMMAND;
                    } else {
                        client.player.sendMessage(Text.of("Skipping empty slot " + currentSlot), false);
                        currentState = State.DELAY;
                    }
                    lastActionTime = currentTime;
                    break;

                case SENDING_COMMAND:
                    // Send the sell command after switching slots
                    String command = "/ah sell " + price;
                    client.player.networkHandler.sendCommand(command.substring(1));
                    client.player.sendMessage(Text.of("Sending sell command for slot " + currentSlot + " with price " + price), false);

                    currentState = State.WAITING_FOR_GUI;
                    lastActionTime = currentTime;
                    break;

                case WAITING_FOR_GUI:
                    // Check if the GUI is open
                    if (client.currentScreen instanceof HandledScreen) {
                        currentState = State.CLICKING_CONFIRM;
                    } else {
                        // If GUI hasn't opened yet, wait a bit longer
                        if (currentTime - lastActionTime > 3000) { // 3 second timeout
                            client.player.sendMessage(Text.of("Timeout waiting for GUI on slot " + currentSlot), false);
                            currentState = State.DELAY;
                            lastActionTime = currentTime;
                        }
                    }
                    break;

                case CLICKING_CONFIRM:
                    if (client.currentScreen instanceof HandledScreen) {
                        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;

                        // Find the confirm button (lime stained glass pane)
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
                        }
                    } else {
                        // GUI was closed unexpectedly
                        currentState = State.DELAY;
                        lastActionTime = currentTime;
                    }
                    break;
            }
        });
    }

    private static int findConfirmButtonSlot(HandledScreen<?> screen) {
        ScreenHandler handler = screen.getScreenHandler();
        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.slots.get(i);
            ItemStack stack = slot.getStack();

            if (stack != null && !stack.isEmpty())
                if (stack.getItem() == Items.LIME_STAINED_GLASS_PANE)
                    return i;
        }

        return -1;
    }

    public static void setState(boolean state, int delay, String targetPrice, int[] endpoints) {
        if (state) {
            triggerActivated = true;
            DELAY_MS = delay;
            price = targetPrice;
            autoSellEndpoints = endpoints;
        }
    }
}