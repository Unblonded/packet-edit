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
import net.minecraft.item.*;
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
import unblonded.packets.cfg;

public class AutoCrystal {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean enabled = true;
    private static PlayerEntity target = null;
    private static BlockPos targetPos = null;
    private static int placeAttempts = 0;
    private static boolean attackLanded = false;
    private static boolean crystalPlaced = false;
    private static long attackTime = 0;
    private static long lastActionTime = 0;
    private static int oldSlot = 0;
    private static boolean wasEnabled = false;


    public static void onInitializeClient() {
        // Trigger on player attack
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

            // If target is invalid, reset
            if (!isValidTarget(target)) {
                reset();
                return;
            }

            // Crystal placement phase
            if (attackLanded && !crystalPlaced) {
                // Predict knockback position
                if (targetPos == null && now - attackTime >= cfg.crystalPlaceTime) {
                    targetPos = findBestPosition(predictKnockbackPosition(target));
                    if (targetPos == null) {
                        reset();
                        return;
                    }
                }

                // Try placing crystal
                if (targetPos != null && now - lastActionTime >= cfg.crystalPlaceTime) {
                    if (tryPlaceCrystal()) {
                        crystalPlaced = true;
                        lastActionTime = now;
                    } else {
                        placeAttempts++;
                        if (placeAttempts >= 3) {
                            reset();
                        } else {
                            targetPos = findBestPosition(predictKnockbackPosition(target));
                            lastActionTime = now;
                        }
                    }
                }
            }

            // Crystal detonation phase
            else if (crystalPlaced && now - lastActionTime >= cfg.crystalAttackTime) {
                if (detonateCrystal()) {
                    reset();
                } else {
                    reset();
                }
            }
        });
    }

    private static BlockPos predictKnockbackPosition(PlayerEntity player) {
        // Calculate predicted position based on player's velocity and knockback
        // This assumes the player is being knocked upward slightly and backward
        Vec3d velocity = player.getVelocity();
        double yOffset = 0.5; // Estimated vertical knockback

        // Get player facing direction to estimate knockback direction
        Vec3d playerLook = mc.player.getRotationVec(1.0F);

        // Calculate predicted position
        return new BlockPos(
                (int) (player.getX() - playerLook.x * 1.5), // Knocked away from attacker
                (int) (player.getY() + yOffset),           // Knocked upward
                (int) (player.getZ() - playerLook.z * 1.5)  // Knocked away from attacker
        );
    }

    private static BlockPos findBestPosition(BlockPos center) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockPos closest = null;
        double minDist = Double.MAX_VALUE;

        // Search in 4 block radius
        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 1; y++) {
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
                mc.world.getBlockState(pos.up(2)).isAir() && // Ensure 2 blocks above are clear
                hasLineOfSight(pos.up());
    }

    private static boolean tryPlaceCrystal() {
        if (!mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
            int crystalSlot = findCrystalSlot();
            if (crystalSlot == -1) return false;
            mc.player.getInventory().selectedSlot = crystalSlot;
            return false;
        }

        // Create hit interaction
        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(targetPos),
                Direction.UP,
                targetPos,
                false
        );

        // Send place packet
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND,
                hit,
                0
        ));

        // Swing hand for visual feedback
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        return true;
    }

    private static boolean detonateCrystal() {
        Entity crystal = findCrystalEntity();
        if (crystal != null) {
            // Switch to weapon if needed
            ensureWeaponSelected();

            // Attack the crystal
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            return true;
        }
        return false;
    }

    private static void ensureWeaponSelected() {
        if (!isHoldingWeapon()) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) {
                    mc.player.getInventory().selectedSlot = i;
                    break;
                }
            }
        }
    }

    private static Entity findCrystalEntity() {
        // Search in a small area around the target position
        Box searchBox = new Box(targetPos).expand(2);
        for (Entity entity : mc.world.getEntitiesByClass(EndCrystalEntity.class, searchBox, e -> true)) {
            if (hasLineOfSight(entity.getBlockPos())) {
                return entity;
            }
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
                player.distanceTo(mc.player) <= 15 && // Within reasonable range
                !player.isSpectator();
    }

    private static void reset() {
        target = null;
        targetPos = null;
        placeAttempts = 0;
        attackLanded = false;
        crystalPlaced = false;
        if (mc.player != null)
            mc.player.getInventory().selectedSlot = oldSlot;
    }

    public static void setState(boolean state) {
        if (!state && wasEnabled) {
            reset(); // Only reset when turning off
        }
        enabled = state;
        wasEnabled = state;
    }
}