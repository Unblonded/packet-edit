package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;


import java.util.*;

public class AutoCrystal {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean enabled = true;
    private static PlayerEntity target = null;
    private static BlockPos targetPos = null;
    private static int actionDelay = 0;
    private static int placeAttempts = 0;
    private static final Random random = new Random();

    public static void onInitializeClient() {
        // Trigger on player attack
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (enabled && entity instanceof PlayerEntity && isHoldingWeapon()) {
                target = (PlayerEntity) entity;
                actionDelay = 3 + random.nextInt(3); // Random initial delay
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || mc.player == null || target == null) return;

            // Action pacing system
            if (actionDelay > 0) {
                actionDelay--;
                return;
            }

            // Find valid position (once per target)
            if (targetPos == null) {
                targetPos = findBestPosition(target.getBlockPos());
                if (targetPos == null) {
                    reset();
                    return;
                }
                placeAttempts = 0;
            }

            // Crystal placement logic
            if (placeAttempts < 3) { // Max 3 attempts
                if (tryPlaceCrystal()) {
                    placeAttempts++;
                    actionDelay = 2 + random.nextInt(4); // Random delay between actions
                } else {
                    reset();
                }
            } else {
                reset();
            }
        });
    }

    private static BlockPos findBestPosition(BlockPos center) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockPos closest = null;
        double minDist = Double.MAX_VALUE;

        // Search in 4 block radius
        for (int x = -4; x <= 4; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -4; z <= 4; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);

                    // Check if valid position
                    if (isValidCrystalPos(pos)) {
                        double dist = pos.getSquaredDistance(center);
                        if (dist < minDist) {
                            minDist = dist;
                            closest = pos.toImmutable();
                        }
                    }
                }
            }
        }
        return closest;
    }

    private static boolean isValidCrystalPos(BlockPos pos) {
        BlockState base = mc.world.getBlockState(pos);
        BlockState above = mc.world.getBlockState(pos.up());

        return (base.isOf(Blocks.OBSIDIAN) || base.isOf(Blocks.BEDROCK)) &&
                above.isAir() &&
                hasLineOfSight(pos.up());
    }

    private static boolean tryPlaceCrystal() {
        if (!mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
            int crystalSlot = findCrystalSlot();
            if (crystalSlot == -1) return false;
            mc.player.getInventory().selectedSlot = crystalSlot;
            actionDelay = 1; // Small delay after switching
            return false;
        }

        // Create fake interaction packet
        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(targetPos.up()),
                Direction.UP,
                targetPos,
                false
        );

        // Send place packet directly (bypasses client-side checks)
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND,
                hit,
                0
        ));

        // Small random delay before detonation
        actionDelay = 1 + random.nextInt(2);

        // Schedule detonation
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        Entity crystal = findCrystalEntity();
        if (crystal != null)
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));


        return true;
    }

    private static Entity findCrystalEntity() {
        Box searchBox = new Box(targetPos.up()).expand(0.5);
        for (Entity entity : mc.world.getEntitiesByClass(EndCrystalEntity.class, searchBox, e -> true)) {
            return entity;
        }
        return null;
    }

    // Helper methods
    private static boolean hasLineOfSight(BlockPos pos) {
        Vec3d start = mc.player.getEyePos();
        Vec3d end = Vec3d.ofCenter(pos);
        return mc.world.raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player
        )).getType() == HitResult.Type.MISS;
    }

    private static int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isHoldingWeapon() {
        Item item = mc.player.getMainHandStack().getItem();
        return item instanceof SwordItem || item instanceof AxeItem;
    }

    private static void reset() {
        target = null;
        targetPos = null;
        placeAttempts = 0;
    }

    public static void setState(boolean state) {
        enabled = state;
    }
}