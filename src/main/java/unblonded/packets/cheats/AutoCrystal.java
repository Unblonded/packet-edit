package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import unblonded.packets.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AutoCrystal implements ClientModInitializer {
    private static final Minecraft client = Minecraft.getInstance();
    private static boolean enabled = true;

    // Cooldown tracking
    private long lastAttackTime = 0;
    private long lastPlaceTime = 0;

    private BlockPos crystalPos = null;
    private int originalSlot = -1;
    private static Player targetPlayer = null;
    private int detonationTimer = 0;

    private static final List<Item> ALLOWED_ITEMS = Arrays.asList(
            Items.END_CRYSTAL,
            Items.DIAMOND_SWORD,
            Items.NETHERITE_SWORD,
            Items.DIAMOND_AXE,
            Items.NETHERITE_AXE
    );

    @Override
    public void onInitializeClient() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof Player && enabled && isHoldingAllowedItem()) {
                targetPlayer = (Player) entity;
                lastAttackTime = System.currentTimeMillis();

                int crystalSlot = findItemSlot(Items.END_CRYSTAL);
                if (crystalSlot != -1 && client.player != null) {
                    originalSlot = client.player.getInventory().selected;
                    client.player.getInventory().selected = crystalSlot;
                }
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!enabled || client.player == null || client.level == null || !isHoldingAllowedItem()) {
                resetState();
                return;
            }

            long currentTime = System.currentTimeMillis();

            // Handle scheduled detonation
            if (detonationTimer > 0) {
                detonationTimer--;
                if (detonationTimer == 0 && crystalPos != null) {
                    detonateCrystal(crystalPos);
                    resetState();
                }
                return;
            }

            // Check valid target
            if (targetPlayer == null || (currentTime - lastAttackTime) > 1000) {
                return;
            }

            // Check attack cooldown
            if ((currentTime - lastAttackTime) < cfg.crystalAttackTime) {
                return;
            }

            // Check placement cooldown
            if ((currentTime - lastPlaceTime) < cfg.crystalPlaceTime) {
                return;
            }

            // Find placement position
            BlockPos bestPlacement = findBestCrystalPosition(targetPlayer.blockPosition());
            if (bestPlacement == null || !client.player.getMainHandItem().is(Items.END_CRYSTAL)) {
                return;
            }

            // Place crystal
            placeCrystal(bestPlacement);
            lastPlaceTime = currentTime;
            crystalPos = bestPlacement.above();

            // Schedule detonation for next tick
            detonationTimer = 1;
        });
    }

    private int findItemSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    private BlockPos findBestCrystalPosition(BlockPos targetPos) {
        BlockPos bestPlacement = null;
        double closestDistance = Double.MAX_VALUE;

        // Get player view vector
        Vec3 playerEyePos = client.player.getEyePosition();
        Vec3 playerLookVec = client.player.getLookAngle();

        // Check blocks in random order to avoid patterns
        List<BlockPos> searchArea = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(targetPos.offset(-4, -1, -4), targetPos.offset(4, 1, 4))) {
            searchArea.add(pos.immutable());
        }
        Collections.shuffle(searchArea);

        for (BlockPos pos : searchArea) {
            BlockState base = client.level.getBlockState(pos);
            BlockState above = client.level.getBlockState(pos.above());
            Vec3 targetVec = Vec3.atCenterOf(pos.above());

            // Check if block is valid and in line of sight
            if ((base.is(Blocks.OBSIDIAN) || base.is(Blocks.BEDROCK)) &&
                    above.isAir() &&
                    client.player.distanceToSqr(targetVec) <= 36 &&
                    hasLineOfSight(playerEyePos, targetVec)) {

                double distance = targetPlayer.distanceToSqr(targetVec);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    bestPlacement = pos.immutable();
                }
            }
        }
        return bestPlacement;
    }

    private boolean hasLineOfSight(Vec3 start, Vec3 end) {
        if (client.level == null) return false;

        // Convert Vec3 to BlockPos for comparison
        BlockPos endPos = new BlockPos((int)Math.floor(end.x), (int)Math.floor(end.y), (int)Math.floor(end.z));

        // Raycast with a small margin to account for block edges
        BlockHitResult hit = client.level.clip(new ClipContext(
                start,
                end.add(0, 0.1, 0), // Slightly above the target position
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                client.player
        ));

        // If raycast hits nothing or hits our target block (or adjacent), we have LOS
        return hit.getType() == HitResult.Type.MISS ||
                hit.getBlockPos().distSqr(endPos) <= 2; // Within 1 block distance
    }

    private void placeCrystal(BlockPos pos) {
        if (!isLookingAt(Vec3.atLowerCornerOf(pos.above()))) return;

        BlockHitResult hitResult = new BlockHitResult(
                Vec3.atCenterOf(pos.above()),
                Direction.UP,
                pos,
                false
        );
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hitResult);
        client.player.swing(InteractionHand.MAIN_HAND);
    }

    private void detonateCrystal(BlockPos pos) {
        if (client.level == null) return;

        // Only detonate if we're looking at the crystal
        for (Entity entity : client.level.getEntities(client.player, new AABB(pos).inflate(1))) {
            if (entity instanceof EndCrystal crystal && isLookingAt(crystal.position())) {
                client.gameMode.attack(client.player, crystal);
                client.player.swing(InteractionHand.MAIN_HAND);
                break;
            }
        }
    }

    private boolean isLookingAt(Vec3 targetPos) {
        Vec3 eyePos = client.player.getEyePosition();
        Vec3 lookVec = client.player.getLookAngle();
        Vec3 targetVec = targetPos.subtract(eyePos).normalize();

        // Check if we're looking roughly at the target (within 30 degrees)
        return lookVec.dot(targetVec) > 0.866; // cos(30°) ≈ 0.866
    }

        private void resetState() {
        if (originalSlot != -1 && client.player != null) {
            client.player.getInventory().selected = originalSlot;
            originalSlot = -1;
        }
        crystalPos = null;
        detonationTimer = 0;
    }

    private boolean isHoldingAllowedItem() {
        Item heldItem = client.player.getMainHandItem().getItem();
        return ALLOWED_ITEMS.contains(heldItem);
    }

    public static void setState(boolean state) {
        enabled = state;
    }
}