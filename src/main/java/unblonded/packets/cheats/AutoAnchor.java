package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import unblonded.packets.util.util;

import java.util.Timer;
import java.util.TimerTask;

public class AutoAnchor implements ClientModInitializer {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private BlockPos anchorPos = null;
    private int glowstoneSlot = -1;
    private int stage = 0;
    private int tickDelay = 0;
    private boolean processing = false;
    private static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        // Listen for anchor placement
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient || hand != Hand.MAIN_HAND || !enabled) return ActionResult.PASS;

            if (player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
                anchorPos = hitResult.getBlockPos().offset(hitResult.getSide());
                glowstoneSlot = findHotbarSlot(Items.GLOWSTONE);
                if (glowstoneSlot != -1) {
                    processing = true;
                    stage = 0;
                    tickDelay = 0;
                }
            }

            return ActionResult.PASS;
        });

        // Tick loop
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!processing || anchorPos == null || client.player == null || client.world == null || !enabled) return;

            processing = false; // Prevent multiple triggers
            new Thread(() -> {
                try {
                    for (int i = 0; i < 4; i++) {
                        Thread.sleep(5 + util.rndInt(3));
                        client.execute(() -> {
                            client.player.getInventory().selectedSlot = glowstoneSlot;
                            interactWithBlock(anchorPos);
                        });
                    }

                    Thread.sleep(5 + util.rndInt(3));
                    client.execute(() -> {
                        int anchorSlot = findHotbarSlot(Items.RESPAWN_ANCHOR);
                        if (anchorSlot != -1) {
                            client.player.getInventory().selectedSlot = anchorSlot;
                            interactWithBlock(anchorPos);
                        }

                        // Reset
                        anchorPos = null;
                        glowstoneSlot = -1;
                        stage = 0;
                    });
                } catch (InterruptedException ignored) { }
            }, "AutoAnchorThread").start();
        });
    }

    private void interactWithBlock(BlockPos pos) {
        BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(pos), // Use the block center instead of eye pos
                Direction.UP,
                pos,
                false
        );

        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    private int findHotbarSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    public static void setState(boolean state) {
        enabled = state;
    }
}
