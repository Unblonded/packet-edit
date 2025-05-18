package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class ForwardTunnel {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static boolean enabled = false;
    private static boolean keyReset = false;
    private static String blockStatus = "";
    private static BlockPos currentMiningPos = null;
    private static Direction currentMiningSide = null;


    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!enabled || client.player == null || client.world == null) return;

            ClientPlayerEntity player = client.player;
            if (!player.isAlive() || player.isSpectator()) {
                disable();
                return;
            }

            // Reset status each tick
            blockStatus = "Mining...";

            // Snap to cardinal direction (even when in GUI)
            snapToCardinalDirection(player);

            // Check if tunneling path is safe
            if (!isTunnelPathSafe(player)) {
                disable();
                return;
            }

            // Look down at a smart angle to hit both eye and floor level blocks
            player.setPitch(35f);

            // Force forward movement directly instead of just pressing keys
            Vec3d forward = Vec3d.fromPolar(0, player.getYaw());
            player.setVelocity(forward.x * 0.1, player.getVelocity().y, forward.z * 0.1);

            // Handle mining/attacking directly
            mineBlocks(player);
        });
    }

    private static void mineBlocks(ClientPlayerEntity player) {
        if (client.interactionManager == null || client.world == null) return;

        Direction facing = player.getHorizontalFacing();
        BlockPos playerPos = player.getBlockPos();

        BlockPos eyePos = playerPos.offset(facing).up();
        BlockPos footPos = playerPos.offset(facing);

        if (currentMiningPos != null) {
            // Keep mining until the block is broken
            if (client.world.getBlockState(currentMiningPos).isAir()) {
                currentMiningPos = null; // Reset when mined
            } else {
                client.interactionManager.updateBlockBreakingProgress(currentMiningPos, currentMiningSide);
                return; // Don’t start a new one yet
            }
        }

        // Try to start mining a new block (top then bottom)
        if (tryStartMining(eyePos, player)) return;
        if (tryStartMining(footPos, player)) return;
    }

    private static boolean tryStartMining(BlockPos pos, ClientPlayerEntity player) {
        BlockState state = client.world.getBlockState(pos);
        if (state.isAir() || state.getFluidState().isStill()) return false;

        // Determine side to hit from player position
        Direction side = Direction.UP;
        Vec3d playerPos = player.getPos();
        if (playerPos.x < pos.getX()) side = Direction.WEST;
        else if (playerPos.x > pos.getX()) side = Direction.EAST;
        else if (playerPos.z < pos.getZ()) side = Direction.NORTH;
        else if (playerPos.z > pos.getZ()) side = Direction.SOUTH;

        // Start mining
        client.interactionManager.attackBlock(pos, side);
        currentMiningPos = pos;
        currentMiningSide = side;
        return true;
    }

    private static void startMining(BlockPos pos, Direction direction) {
        if (!pos.equals(currentMiningPos)) {
            client.interactionManager.attackBlock(pos, direction);
            client.player.swingHand(Hand.MAIN_HAND);
            currentMiningPos = pos;
        }

        client.interactionManager.updateBlockBreakingProgress(pos, direction);
    }

    private static void stopMining() {
        currentMiningPos = null;
    }

    private static boolean shouldMine(BlockPos pos) {
        BlockState state = client.world.getBlockState(pos);
        return !state.isAir() && state.getHardness(client.world, pos) >= 0;
    }

    private static void snapToCardinalDirection(PlayerEntity player) {
        float yaw = (player.getYaw() % 360 + 360) % 360;

        float snappedYaw;
        if (yaw >= 45 && yaw < 135) {
            snappedYaw = 90; // East
        } else if (yaw >= 135 && yaw < 225) {
            snappedYaw = 180; // South
        } else if (yaw >= 225 && yaw < 315) {
            snappedYaw = 270; // West
        } else {
            snappedYaw = 0; // North
        }

        player.setYaw(snappedYaw);
        player.setHeadYaw(snappedYaw);
    }

    private static boolean isTunnelPathSafe(PlayerEntity player) {
        Direction facing = player.getHorizontalFacing();
        BlockPos playerPos = player.getBlockPos();

        for (int i = 1; i <= 2; i++) {
            BlockPos ahead = playerPos.offset(facing, i);

            // Danger check
            if (isDangerousBlock(ahead.up())) {
                blockStatus = "Danger block ahead!";
                return false;
            }

            // Floor check
            BlockPos below = ahead.down();
            BlockState belowState = client.world.getBlockState(below);
            if (belowState.isAir() || !belowState.getFluidState().isEmpty()) {
                blockStatus = "No floor!";
                return false;
            }
        }

        return true;
    }

    private static boolean isDangerousBlock(BlockPos pos) {
        BlockState state = client.world.getBlockState(pos);
        return state.isOf(Blocks.LAVA) ||
                state.isOf(Blocks.FIRE) ||
                state.isOf(Blocks.SOUL_FIRE) ||
                state.isOf(Blocks.MAGMA_BLOCK);
    }

    public static void setState(boolean state) {
        enabled = state;
        keyReset = false;

        if (enabled) {
            blockStatus = "ForwardTunnel enabled.";
        } else {
            disable();
        }
    }

    public static void disable() {
        if (!enabled) return;
        enabled = false;
        blockStatus = "ForwardTunnel disabled.";

        if (!keyReset && client.options != null) {
            KeyBinding.setKeyPressed(client.options.forwardKey.getDefaultKey(), false);
            KeyBinding.setKeyPressed(client.options.attackKey.getDefaultKey(), false);
            keyReset = true;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getBlockStatus() {
        return blockStatus;
    }
}
