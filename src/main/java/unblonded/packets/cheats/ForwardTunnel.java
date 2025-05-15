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
    private static int miningTicks = 0;

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
        if (client.interactionManager == null) return;

        Direction facing = player.getHorizontalFacing();
        BlockPos playerPos = player.getBlockPos();

        // Mine at eye level
        BlockPos eyePos = playerPos.offset(facing).up();
        attemptMineBlock(eyePos);

        // Mine at foot level
        BlockPos footPos = playerPos.offset(facing);
        attemptMineBlock(footPos);
    }

    private static void attemptMineBlock(BlockPos pos) {
        if (client.world == null || client.interactionManager == null) return;

        BlockState state = client.world.getBlockState(pos);
        if (!state.isAir() && !state.getFluidState().isStill()) {
            // Create hit vector targeting center of block
            Vec3d hitVec = new Vec3d(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5
            );

            Direction side = Direction.UP; // Default side

            // Calculate side based on player position
            Vec3d playerPos = client.player.getPos();
            if (playerPos.x < pos.getX()) side = Direction.WEST;
            else if (playerPos.x > pos.getX()) side = Direction.EAST;
            else if (playerPos.z < pos.getZ()) side = Direction.NORTH;
            else if (playerPos.z > pos.getZ()) side = Direction.SOUTH;

            BlockHitResult hit = new BlockHitResult(hitVec, side, pos, false);

            // Start breaking and continue breaking regardless of GUI state
            if (miningTicks == 0) {
                client.interactionManager.attackBlock(pos, side);
            }

            // Continue to mine by simulating progression
            client.interactionManager.updateBlockBreakingProgress(pos, side);
            miningTicks = (miningTicks + 1) % 5; // Reset periodically to ensure continuous mining
        }
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

    public static void toggle() {
        setState(!enabled);
    }

    public static void setState(boolean state) {
        enabled = state;
        keyReset = false;
        miningTicks = 0;

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
