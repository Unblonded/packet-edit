package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import unblonded.packets.cfg;
import unblonded.packets.imgui.Alert;

public class TotemNotifier {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // Static fields to track state between ticks
    private static boolean hadTotemLastTick = false;
    private static float lastHealth = 0;
    private static boolean alreadyAlerted = false; // Prevent multiple alerts for same pop

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (MC.player != null && MC.world != null)
                if (cfg.totemNotifier.get() && checkForTotemPop())
                    Alert.warning("Totem Notifier", "Totem popped! Health: " + MC.player.getHealth());
        });
    }

    public static boolean checkForTotemPop() {
        if (MC.player == null || MC.world == null || MC.player.isCreative() || MC.player.isSpectator()) return false;

        ItemStack offhandStack = MC.player.getStackInHand(Hand.OFF_HAND);
        boolean hasTotemNow = offhandStack.isOf(Items.TOTEM_OF_UNDYING);
        float currentHealth = MC.player.getHealth();

        if (hadTotemLastTick && !hasTotemNow && !alreadyAlerted) {
            boolean totemPopped = lastHealth <= 2.0f ||
                    currentHealth < lastHealth - 6.0f ||
                    MC.player.hurtTime > 0;

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