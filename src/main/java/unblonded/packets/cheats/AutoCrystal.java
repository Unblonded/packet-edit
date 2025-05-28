package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.awt.event.InputEvent;

public class AutoCrystal {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean enabled = true;
    private static PlayerEntity target = null;
    private static BlockPos targetPos = null;
    private static boolean attackLanded = false;
    private static long attackTime = 0;
    private static int oldSlot = 0;
    private static boolean wasEnabled = false;

    public static void onInitializeClient() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (enabled && entity instanceof PlayerEntity && isHoldingWeapon()) {
                target = (PlayerEntity) entity;
                attackLanded = true;
                attackTime = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || mc.player == null || target == null) return;

            long now = System.currentTimeMillis();

            if (!isValidTarget(target)) {
                reset();
                return;
            }

            if (attackLanded && now - attackTime >= 50) {
                targetPos = findBestPosition(predictKnockbackPosition(target));
                if (targetPos == null) {
                    reset();
                    return;
                }

                lookAtBlock(targetPos);
                int crystalSlot = findCrystalSlot();
                if (crystalSlot != -1) {
                    mc.player.getInventory().selectedSlot = crystalSlot;
                }

                mc.options.useKey.setPressed(true);
                attackLanded = false;
            } else if (targetPos != null) {
                Vec3d eyes = mc.player.getEyePos();
                Vec3d lookVec = mc.player.getRotationVec(1.0F);
                Vec3d targetVec = Vec3d.ofCenter(targetPos);

                Vec3d directionToTarget = targetVec.subtract(eyes).normalize();
                double dot = lookVec.dotProduct(directionToTarget);
                if (dot < 0.90) {
                    mc.options.useKey.setPressed(false);
                    targetPos = null;
                } else {
                    mc.options.useKey.setPressed(true);
                }
            } else {
                mc.options.useKey.setPressed(false);
            }
        });

    }

    private static BlockPos predictKnockbackPosition(PlayerEntity player) {
        double yOffset = 0.5;
        Vec3d playerLook = mc.player.getRotationVec(1.0F);

        return new BlockPos(
                (int) (player.getX() - playerLook.x * 1.5),
                (int) (player.getY() + yOffset),
                (int) (player.getZ() - playerLook.z * 1.5)
        );
    }

    private static BlockPos findBestPosition(BlockPos center) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockPos closest = null;
        double minDist = Double.MAX_VALUE;

        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 1; y++) {
                for (int z = -4; z <= 4; z++) {
                    pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);

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
                mc.world.getBlockState(pos.up(2)).isAir() &&
                hasLineOfSight(pos.up());
    }

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
                oldSlot = mc.player.getInventory().selectedSlot;
                return i;
            }
        }
        return -1;
    }

    private static boolean isHoldingWeapon() {
        Item item = mc.player.getMainHandStack().getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof EndCrystalItem;
    }

    private static boolean isValidTarget(PlayerEntity player) {
        return player != null && player.isAlive() &&
                player.distanceTo(mc.player) <= 15 &&
                !player.isSpectator();
    }

    private static void reset() {
        target = null;
        targetPos = null;
        attackLanded = false;
        if (mc.player != null)
            mc.player.getInventory().selectedSlot = oldSlot;
    }

    public static void setState(boolean state) {
        if (!state && wasEnabled) {
            reset();
        }
        enabled = state;
        wasEnabled = state;
    }

    private static void lookAtBlock(BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d hit = Vec3d.ofCenter(pos);
        float[] angles = getRotations(eyes, hit);
        mc.player.setYaw(angles[0]);
        mc.player.setPitch(angles[1]);
    }

    private static float[] getRotations(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        return new float[]{yaw, pitch};
    }
}