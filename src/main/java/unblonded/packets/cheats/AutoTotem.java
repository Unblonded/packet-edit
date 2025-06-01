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

public class AutoTotem {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    
    private static boolean enabled = true;

    private static boolean hadTotemLastTick = false;
    private static boolean intentionalSwitch = false;
    private static boolean justSwitchedItem = false;
    
    private static Timer reequipTimer;
    private static boolean reequipScheduled = false;

    private static long reequipDelayMs = 500;

    private static float lastHealth = 0;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MC.player != null) {
                if (MC.options.useKey.wasPressed() || MC.options.attackKey.wasPressed() ||
                        MC.options.pickItemKey.wasPressed() || MC.options.swapHandsKey.wasPressed()) {
                    intentionalSwitch = true;
                }
            }
            tickAutoTotem();
        });
    }

    private static void tickAutoTotem() {
        
        if (!enabled || MC.player == null || MC.world == null || MC.player.isCreative() || MC.player.isSpectator()) return;
        if (justSwitchedItem) justSwitchedItem = false;

        ItemStack offhandStack = MC.player.getStackInHand(Hand.OFF_HAND);
        boolean hasTotemNow = offhandStack.isOf(Items.TOTEM_OF_UNDYING);
        float currentHealth = MC.player.getHealth();

        
        if (hadTotemLastTick && !hasTotemNow) {

            boolean likelyTotemPop = lastHealth <= 2.0f ||
                    currentHealth < lastHealth - 6.0f ||
                    MC.player.hurtTime > 0;

            if (likelyTotemPop && !intentionalSwitch)
                if (!reequipScheduled)
                    scheduleTotemReequip();
            else {
                intentionalSwitch = true;
                justSwitchedItem = true;
            }
        }

        hadTotemLastTick = hasTotemNow;
        lastHealth = currentHealth;
        
        if (intentionalSwitch && !justSwitchedItem) {
            intentionalSwitch = false;
        }
    }

    private static void scheduleTotemReequip() {
        if (reequipTimer != null) {
            reequipTimer.cancel();
        }

        reequipTimer = new Timer();
        reequipScheduled = true;

        if (MC.player != null)
            MC.player.sendMessage(Text.literal("§eTotem popped! Will reequip in " + (reequipDelayMs / 1000.0) + " seconds..."), true);

        
        reequipTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                MC.execute(() -> {
                    tryReequipTotem();
                    reequipScheduled = false;
                });
            }
        }, reequipDelayMs);
    }

    private static void tryReequipTotem() {
        if (MC.player == null) return;

        ItemStack offhandStack = MC.player.getStackInHand(Hand.OFF_HAND);
        
        if (offhandStack.isEmpty()) {
            int totemSlot = findTotemInInventory();
            if (totemSlot != -1) {
                moveTotemToOffhand(totemSlot);

                if (MC.player != null) MC.player.sendMessage(Text.literal("§aAuto Totem: Equipped new totem"), true);
            } else if (MC.player != null) MC.player.sendMessage(Text.literal("§cAuto Totem: No totems found in inventory"), true);
        }
    }

    private static int findTotemInInventory() {
        
        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) return i < 9 ? i + 36 : i;
        }
        return -1; 
    }

    private static void moveTotemToOffhand(int totemSlot) {
        if (MC.interactionManager != null && MC.player.currentScreenHandler == MC.player.playerScreenHandler) {
            MC.interactionManager.clickSlot(
                    MC.player.playerScreenHandler.syncId,
                    totemSlot,
                    0, 
                    SlotActionType.PICKUP, 
                    MC.player
            );
            MC.interactionManager.clickSlot(
                    MC.player.playerScreenHandler.syncId,
                    45,
                    0, 
                    SlotActionType.PICKUP, 
                    MC.player
            );
        }
    }

    public static void setState(boolean state, int delayMs, int humanityMs) {
        enabled = state;
        if (humanityMs != 0){
            int delay = util.rndInt(humanityMs);
            if (delayMs + delay <= 0)
                reequipDelayMs = delayMs + delay + 10;
            else reequipDelayMs = delayMs + delay;
        }
        else reequipDelayMs = delayMs;
    }
}