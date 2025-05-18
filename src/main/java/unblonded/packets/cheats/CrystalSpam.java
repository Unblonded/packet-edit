package unblonded.packets.cheats;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CrystalSpam {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static int searchRadius = 5;
    private static int breakDelay = 10;
    private static boolean isRunning = false;
    private static boolean enabled = false;

    public static void setState(boolean state, int radius, int delay) {
        enabled = state;
        breakDelay = delay;
        searchRadius = radius;
    }

    public static void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        new Thread(CrystalSpam::run).start();
    }


    private static void run() {
        while (isRunning) {
            try {
                if (mc.player == null || mc.world == null || !enabled || !mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
                    Thread.sleep(500);
                    continue;
                }

                Optional<BlockPos> targetBlockPos = findClosestValidBlock();

                if (targetBlockPos.isPresent()) {
                    BlockPos pos = targetBlockPos.get();
                    if (mc.player.getY() < pos.getY() + 1) {
                        placeCrystal(pos);
                        Thread.sleep(breakDelay/2);
                        breakCrystal(pos);

                        Thread.sleep(breakDelay);
                    }
                }

                Thread.sleep(50); // Small delay to prevent excessive CPU usage
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000); // Wait before retrying after an error
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static Optional<BlockPos> findClosestValidBlock() {
        PlayerEntity player = mc.player;
        World world = mc.world;

        if (player == null || world == null) {
            return Optional.empty();
        }

        List<BlockPos> validBlocks = new ArrayList<>();
        BlockPos playerPos = player.getBlockPos();

        // Scan in a cube around the player
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = world.getBlockState(pos).getBlock();

                    // Check if block is obsidian or bedrock
                    if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                        // Check if there's air above it (for crystal placement)
                        BlockPos abovePos = pos.up();
                        if (world.getBlockState(abovePos).isAir() && world.getBlockState(abovePos.up()).isAir()) {
                            validBlocks.add(pos);
                        }
                    }
                }
            }
        }

        // Sort blocks by distance to player
        return validBlocks.stream()
                .min(Comparator.comparingDouble(pos ->
                        player.getPos().squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
    }

    /**
     * Places an End Crystal on the specified block
     * @param pos Position to place the crystal on
     */
    private static void placeCrystal(BlockPos pos) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Find end crystals in hotbar
        int crystalSlot = findItemInHotbar(Items.END_CRYSTAL);
        if (crystalSlot == -1) {
            return; // No crystals found
        }

        // Save the current selected slot
        int originalSlot = mc.player.getInventory().selectedSlot;

        // Select the crystal
        mc.player.getInventory().selectedSlot = crystalSlot;

        // Calculate the position and look at it
        Vec3d placePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        lookAt(placePos);

        // Place the crystal
        BlockHitResult hitResult = new BlockHitResult(placePos, Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

        // Restore original slot
        mc.player.getInventory().selectedSlot = originalSlot;
    }

    private static void breakCrystal(BlockPos pos) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Find the closest crystal entity near the block
        mc.world.getEntitiesByClass(net.minecraft.entity.decoration.EndCrystalEntity.class,
                        mc.player.getBoundingBox().expand(searchRadius),
                        entity -> {
                            double distX = Math.abs(entity.getX() - (pos.getX() + 0.5));
                            double distY = Math.abs(entity.getY() - (pos.getY() + 1.0));
                            double distZ = Math.abs(entity.getZ() - (pos.getZ() + 0.5));
                            return distX < 0.5 && distY < 1.0 && distZ < 0.5;
                        })
                .stream()
                .findFirst()
                .ifPresent(crystal -> {
                    // Look at the crystal
                    lookAt(crystal.getPos());

                    // Attack the crystal
                    mc.interactionManager.attackEntity(mc.player, crystal);
                    mc.player.swingHand(Hand.MAIN_HAND);
                });
    }

    private static int findItemInHotbar(net.minecraft.item.Item item) {
        if (mc.player == null) {
            return -1;
        }

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    private static void lookAt(Vec3d pos) {
        if (mc.player == null) {
            return;
        }

        Vec3d playerPos = mc.player.getEyePos();
        Vec3d direction = pos.subtract(playerPos).normalize();

        double pitch = Math.asin(-direction.y) * 180.0 / Math.PI;
        double yaw = Math.atan2(direction.z, direction.x) * 180.0 / Math.PI - 90.0;

        // Set the player's rotation
        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }
}