package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import unblonded.packets.cfg;
import unblonded.packets.imgui.Alert;

public class TotemNotifier {
    private static boolean hadTotemLastTick = false;
    private static float lastHealth = 0;
    private static boolean alreadyAlerted = false;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null)
                if (cfg.totemNotifier.get() && checkForTotemPop(client))
                    Alert.warning("Totem Notifier", "Totem popped! Health: " + client.player.getHealth());
        });
    }

    public static boolean checkForTotemPop(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.player.isCreative() || mc.player.isSpectator()) return false;

        ItemStack offhandStack = mc.player.getStackInHand(Hand.OFF_HAND);
        boolean hasTotemNow = offhandStack.isOf(Items.TOTEM_OF_UNDYING);
        float currentHealth = mc.player.getHealth();

        if (hadTotemLastTick && !hasTotemNow && !alreadyAlerted) {
            boolean totemPopped = lastHealth <= 2.0f ||
                    currentHealth < lastHealth - 6.0f ||
                    mc.player.hurtTime > 0;

            if (totemPopped) {
                alreadyAlerted = true;
                hadTotemLastTick = hasTotemNow;
                lastHealth = currentHealth;
                return true;
            }
        }

        if (!hadTotemLastTick && hasTotemNow) alreadyAlerted = false;

        hadTotemLastTick = hasTotemNow;
        lastHealth = currentHealth;

        return false;
    }
}