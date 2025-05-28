package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import unblonded.packets.cfg;
import unblonded.packets.util.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SelfCrystal {
    private static final Map<BlockPos, CrystalInfo> trackedCrystals = new ConcurrentHashMap<>();
    private static boolean enabled = true;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static class CrystalInfo {
        long placedTime;
        int ticksWaited;
        int attackDelay;
        boolean readyToAttack;

        CrystalInfo() {
            this.placedTime = System.currentTimeMillis();
            this.ticksWaited = 0;
            this.attackDelay = cfg.selfCrystalDelay[0] + util.rndInt(cfg.autoAnchorHumanity[0]);
            this.readyToAttack = false;
        }
    }

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            onTick();
        });
    }

    public static void clearAll() {
        trackedCrystals.clear();
    }

    public static void onPlayerInteract(ItemStack stack, Hand hand, BlockHitResult hitResult) {
        if (!enabled || mc.player == null || mc.world == null) return;

        if (stack.getItem() instanceof EndCrystalItem) {
            BlockPos hitPos = hitResult.getBlockPos();
            Direction face = hitResult.getSide();

            // Calculate all possible crystal positions
            List<BlockPos> possiblePositions = new ArrayList<>();

            if (face == Direction.UP) {
                possiblePositions.add(hitPos.up());
            } else {
                possiblePositions.add(hitPos.offset(face));
            }

            // Also check adjacent positions in case of slight placement variations
            BlockPos primary = possiblePositions.get(0);
            possiblePositions.add(primary.add(1, 0, 0));
            possiblePositions.add(primary.add(-1, 0, 0));
            possiblePositions.add(primary.add(0, 0, 1));
            possiblePositions.add(primary.add(0, 0, -1));

            // Track all possible positions
            for (BlockPos pos : possiblePositions) {
                trackedCrystals.put(pos, new CrystalInfo());
            }
        }
    }

    private static void onTick() {
        if (!enabled || mc.player == null || mc.world == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, CrystalInfo>> iterator = trackedCrystals.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrystalInfo> entry = iterator.next();
            BlockPos pos = entry.getKey();
            CrystalInfo info = entry.getValue();

            info.ticksWaited++;

            // Remove if too old (10 seconds)
            if (currentTime - info.placedTime > 10000) {
                iterator.remove();
                continue;
            }

            // Find crystal at this position
            EndCrystalEntity crystal = findCrystalAtPos(pos);

            if (crystal == null) {
                // If we've waited a reasonable time and still no crystal, remove this position
                if (info.ticksWaited > 20) { // 1 second
                    iterator.remove();
                }
                continue;
            }

            // Check if enough time has passed to attack
            if (!info.readyToAttack && currentTime - info.placedTime >= info.attackDelay) {
                info.readyToAttack = true;
            }

            // Attack if ready
            if (info.readyToAttack) {
                attackCrystal(crystal);
                iterator.remove(); // Remove after attacking

                // Remove other positions tracking the same crystal to avoid duplicate attacks
                removeOtherPositionsForSameArea(pos);
            }
        }
    }

    private static void removeOtherPositionsForSameArea(BlockPos centerPos) {
        Iterator<Map.Entry<BlockPos, CrystalInfo>> iterator = trackedCrystals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrystalInfo> entry = iterator.next();
            BlockPos pos = entry.getKey();

            // Remove positions within 2 blocks of the attacked position
            if (!pos.equals(centerPos) && pos.isWithinDistance(centerPos, 2.0)) {
                iterator.remove();
            }
        }
    }

    private static EndCrystalEntity findCrystalAtPos(BlockPos pos) {
        if (mc.world == null) return null;

        // Expanded search box to catch crystals that might be slightly offset
        Box searchBox = new Box(
                pos.getX() - 1.0, pos.getY() - 0.5, pos.getZ() - 1.0,
                pos.getX() + 2.0, pos.getY() + 2.5, pos.getZ() + 2.0
        );

        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(
                EndCrystalEntity.class,
                searchBox,
                entity -> entity.isAlive()
        );

        if (crystals.isEmpty()) {
            return null;
        }

        // Return the closest crystal to the expected position
        EndCrystalEntity closest = crystals.get(0);
        double closestDistance = closest.getPos().distanceTo(pos.toCenterPos());

        for (EndCrystalEntity crystal : crystals) {
            double distance = crystal.getPos().distanceTo(pos.toCenterPos());
            if (distance < closestDistance) {
                closest = crystal;
                closestDistance = distance;
            }
        }

        return closest;
    }

    private static void attackCrystal(Entity crystal) {
        if (mc.player == null || mc.interactionManager == null || crystal == null) {
            return;
        }

        if (!crystal.isAlive()) {
            return;
        }

        try {
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
        } catch (Exception ignored) { }
    }

    public static void setState(boolean state) {
        if (state && !enabled) {
            enabled = true;
        } else if (!state && enabled) {
            enabled = false;
            clearAll();
        }
    }
}