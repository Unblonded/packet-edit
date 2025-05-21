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
    private static final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Timer> explosionTimers = new ConcurrentHashMap<>();

    private static boolean enabled = true;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            onTick();
        });
    }

    public static void clearAll() {
        for (Timer timer : explosionTimers.values())
            timer.cancel();

        explosionTimers.clear();
        placedCrystals.clear();
    }

    public static void onPlayerInteract(ItemStack stack, Hand hand, BlockHitResult hitResult) {
        if (!enabled || mc.player == null) return;

        if (stack.getItem() instanceof EndCrystalItem) {
            BlockPos hitPos = hitResult.getBlockPos();
            Direction face = hitResult.getSide();

            BlockPos crystalPos;
            if (face == Direction.UP) crystalPos = hitPos.up();
            else crystalPos = hitPos.offset(face);

            final BlockPos finalCrystalPos = crystalPos;

            java.util.Timer delayTimer = new Timer(true);
            delayTimer.schedule(new TimerTask() {
                @Override public void run() {
                    trackCrystalPlacement(finalCrystalPos);
                }
            }, 50);
        }
    }

    private static void trackCrystalPlacement(BlockPos pos) {
        if (!enabled) return;

        placedCrystals.put(pos, System.currentTimeMillis());
        Timer explosionTimer = new Timer(true);

        if (explosionTimers.containsKey(pos))
            explosionTimers.get(pos).cancel();

        explosionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!enabled || mc.player == null) return;

                EndCrystalEntity crystal = findCrystalAtPos(pos);
                if (crystal != null) {
                    attackCrystal(crystal);
                }

                placedCrystals.remove(pos);
                explosionTimers.remove(pos);
            }
        }, cfg.selfCrystalDelay + util.rndInt(cfg.autoAnchorHumanity));

        explosionTimers.put(pos, explosionTimer);
    }

    private static void onTick() {
        if (!enabled || mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, Long>> it = placedCrystals.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<BlockPos, Long> entry = it.next();
            if (currentTime - entry.getValue() > 10000 || findCrystalAtPos(entry.getKey()) == null) {
                if (explosionTimers.containsKey(entry.getKey())) {
                    explosionTimers.get(entry.getKey()).cancel();
                    explosionTimers.remove(entry.getKey());
                }
                it.remove();
            }
        }
    }

    private static EndCrystalEntity findCrystalAtPos(BlockPos pos) {
        if (mc.world == null) return null;

        Box searchBox = new Box(
                pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5,
                pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5
        );
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(
                EndCrystalEntity.class,
                searchBox,
                entity -> true
        );

        return crystals.isEmpty() ? null : crystals.get(0);
    }

    private static void attackCrystal(Entity crystal) {
        if (mc.player == null || mc.interactionManager == null) return;

        mc.interactionManager.attackEntity(mc.player, crystal);
        mc.player.swingHand(Hand.MAIN_HAND);
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