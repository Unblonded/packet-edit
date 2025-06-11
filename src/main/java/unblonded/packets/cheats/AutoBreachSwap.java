package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class AutoBreachSwap {

    public static boolean enabled = false; // <--- Your toggle

    private static boolean shouldSwap = false;
    private static int originalSlot = -1;
    private static int maceSlot = -1;
    private static int delayTicks = 0;

    public static void onInitializeClient() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!enabled || world.isClient == false || hand != Hand.MAIN_HAND) return ActionResult.PASS;

            if (!(player.getMainHandStack().getItem() instanceof MaceItem)) {
                maceSlot = findMaceSlot(player);
                if (maceSlot != -1 && maceSlot != player.getInventory().selectedSlot) {
                    originalSlot = player.getInventory().selectedSlot;
                    shouldSwap = true;
                    delayTicks = 1;
                }
            }

            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.player == null || client.world == null) return;

            if (shouldSwap) {
                if (delayTicks > 0) {
                    delayTicks--;
                    return;
                }

                // Swap to mace and attack
                client.player.getInventory().selectedSlot = maceSlot;

                delayTicks = 1;
                shouldSwap = false;

            } else if (delayTicks == 0 && originalSlot != -1) {
                client.player.getInventory().selectedSlot = originalSlot;
                originalSlot = -1;
            }
        });
    }

    private static int findMaceSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof MaceItem) {
                return i;
            }
        }
        return -1;
    }
}