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
        int attackAttempts;
        long lastAttackTime;
        static final int MAX_ATTACK_ATTEMPTS = 10; // Maximum attempts before giving up
        static final int ATTACK_RETRY_DELAY = 100; // ms between attack attempts

        CrystalInfo() {
            this.placedTime = System.currentTimeMillis();
            this.ticksWaited = 0;
            this.attackDelay = cfg.selfCrystalDelay[0] + util.rndInt(cfg.selfCrystalHumanity[0]);
            this.readyToAttack = false;
            this.attackAttempts = 0;
            this.lastAttackTime = 0;
        }
    }

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> { onTick(); });
    }

    public static void clearAll() {
        trackedCrystals.clear();
    }

    public static void onPlayerInteract(ItemStack stack, Hand hand, BlockHitResult hitResult) {
        if (!enabled || mc.player == null || mc.world == null) return;

        if (stack.getItem() instanceof EndCrystalItem) {
            BlockPos hitPos = hitResult.getBlockPos();
            Direction face = hitResult.getSide();

            List<BlockPos> possiblePositions = new ArrayList<>();

            if (face == Direction.UP) possiblePositions.add(hitPos.up());
            else possiblePositions.add(hitPos.offset(face));

            BlockPos primary = possiblePositions.get(0);
            possiblePositions.add(primary.add(1, 0, 0));
            possiblePositions.add(primary.add(-1, 0, 0));
            possiblePositions.add(primary.add(0, 0, 1));
            possiblePositions.add(primary.add(0, 0, -1));


            for (BlockPos pos : possiblePositions) {
                trackedCrystals.put(pos, new CrystalInfo());
            }
        }
    }

    private static void onTick() {
        if (!enabled || mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, CrystalInfo>> iterator = trackedCrystals.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrystalInfo> entry = iterator.next();
            BlockPos pos = entry.getKey();
            CrystalInfo info = entry.getValue();

            info.ticksWaited++;

            // Extended timeout for laggy servers
            if (currentTime - info.placedTime > 15000) {
                iterator.remove();
                continue;
            }

            EndCrystalEntity crystal = findCrystalAtPos(pos);

            // If crystal is gone, check if we successfully broke it
            if (crystal == null) {
                if (info.attackAttempts > 0) {
                    // Crystal was attacked and is now gone - success!
                    iterator.remove();
                    removeOtherPositionsForSameArea(pos);
                } else if (info.ticksWaited > 20) {
                    // Crystal never appeared or disappeared before we could attack
                    iterator.remove();
                }
                continue;
            }

            // Initial delay before first attack
            if (!info.readyToAttack && currentTime - info.placedTime >= info.attackDelay) {
                info.readyToAttack = true;
            }

            // Keep attacking until crystal is destroyed
            if (info.readyToAttack) {
                // Check if enough time has passed since last attack attempt
                if (currentTime - info.lastAttackTime >= (cfg.selfCrystalDelay[0] + util.rndInt(cfg.selfCrystalHumanity[0]))) {

                    // Give up if we've tried too many times
                    if (info.attackAttempts >= CrystalInfo.MAX_ATTACK_ATTEMPTS) {
                        iterator.remove();
                        continue;
                    }

                    // Verify crystal is still alive before attacking
                    if (crystal.isAlive()) {
                        attackCrystal(crystal);
                        info.attackAttempts++;
                        info.lastAttackTime = currentTime;
                    } else {
                        // Crystal is dead, we're done
                        iterator.remove();
                        removeOtherPositionsForSameArea(pos);
                    }
                }
                // Don't remove here - keep trying until crystal is actually gone
            }
        }
    }

    private static void removeOtherPositionsForSameArea(BlockPos centerPos) {
        Iterator<Map.Entry<BlockPos, CrystalInfo>> iterator = trackedCrystals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CrystalInfo> entry = iterator.next();
            BlockPos pos = entry.getKey();

            if (!pos.equals(centerPos) && pos.isWithinDistance(centerPos, 2.0)) {
                iterator.remove();
            }
        }
    }

    private static EndCrystalEntity findCrystalAtPos(BlockPos pos) {
        if (mc.world == null) return null;

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