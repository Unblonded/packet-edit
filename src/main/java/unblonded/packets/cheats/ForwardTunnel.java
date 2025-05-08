package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import unblonded.packets.util.GameModeAccessor;

public class ForwardTunnel implements ClientModInitializer {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static boolean enabled = true;
    private static boolean keyReset = false;
    private static float targetYaw = -1f;
    private static final float SMOOTHING_SPEED = 1.0f;
    private static String blockStatus = "";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!enabled || client.player == null || client.world == null) return;

            PlayerEntity player = client.player;
            if (!player.isAlive() || player.isSpectator()) return;

            setHeadPos();
            updateHeadRotation();

            Vec3d start = player.getEyePos();
            Vec3d look = player.getRotationVec(1.0f).normalize();
            Vec3d end = start.add(look.multiply(5.0));

            BlockHitResult hit = client.world.raycast(new RaycastContext(
                    start, end,
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE,
                    player
            ));

            GameModeAccessor accessor = (GameModeAccessor) client.interactionManager;
            BlockPos pos = accessor.getDestroyBlockPos();
            float progress = accessor.getDestroyProgress();

            if (client.world.getBlockState(pos).getBlock() != Blocks.AIR)
                blockStatus = "Mining -> " + client.world.getBlockState(pos).getBlock().getName().getString() + " | " + String.format("%.0f", progress * 100) + "%";

            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = hit.getBlockPos();

                if (!isTunnelPathSafe(player, targetPos, look)) {
                    disable();
                    return;
                }

                KeyBinding.setKeyPressed(client.options.forwardKey.getDefaultKey(), true); // Start moving forward
                client.interactionManager.updateBlockBreakingProgress(targetPos, hit.getSide());
                player.swingHand(player.getActiveHand());
            }
        });
    }

    /**
     * Checks if the block ahead and its surroundings are safe for mining and walking into.
     */
    private boolean isTunnelPathSafe(PlayerEntity player, BlockPos target, Vec3d lookVec) {
        // Blocks directly in the path
        for (int i = 0; i <= 2; i++) {
            Vec3d offset = lookVec.multiply(i + 1);
            BlockPos ahead = target.add((int) offset.x, (int) offset.y, (int) offset.z);

            if (isDangerousBlock(ahead)) return false;

            // Ensure the block below is not air or liquid
            BlockPos below = ahead.down();
            BlockState belowState = client.world.getBlockState(below);
            if (belowState.isAir() || isDangerousBlock(below)) return false;
        }

        // Prevent falling into holes
        BlockPos afterBreak = target.offset(player.getHorizontalFacing().getOpposite());
        BlockPos belowAfterBreak = afterBreak.down();
        BlockState stateBelow = client.world.getBlockState(belowAfterBreak);
        if (stateBelow.isAir() || isDangerousBlock(belowAfterBreak)) return false;

        return true;
    }

    /**
     * Determines if the block at a given position is dangerous.
     */
    private boolean isDangerousBlock(BlockPos pos) {
        BlockState state = client.world.getBlockState(pos);
        return state.isAir()
                || state.isOf(Blocks.LAVA)
                || state.isOf(Blocks.WATER)
                || state.isOf(Blocks.FIRE)
                || state.isOf(Blocks.CACTUS)
                || state.isOf(Blocks.MAGMA_BLOCK)
                || state.isOf(Blocks.VOID_AIR);
    }

    public static void setState(boolean state) {
        if (state) keyReset = false;
        enabled = state;
        if (!state) disable();
    }

    public static void updateHeadRotation() {
        if (client.player == null) return;

        if (targetYaw == -1f) return; // No target set

        float currentYaw = normalizeYaw(client.player.getYaw());

        float delta = getShortestAngleDiff(currentYaw, targetYaw);

        // Stop rotating if close enough
        if (Math.abs(delta) < 1.0f) {
            client.player.setYaw(targetYaw);
            client.player.setHeadYaw(targetYaw);
            targetYaw = -1f;
            return;
        }

        // Interpolate yaw
        float newYaw = normalizeYaw(currentYaw + Math.signum(delta) * Math.min(SMOOTHING_SPEED, Math.abs(delta)));
        client.player.setYaw(newYaw);
        client.player.setHeadYaw(newYaw);
    }

    public static void setHeadPos() {
        if (client.player == null) return;

        float yaw = normalizeYaw(client.player.getYaw());

        // Snap to nearest cardinal direction
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

        targetYaw = snappedYaw;
    }

    // Utility to wrap yaw to 0–360
    private static float normalizeYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    // Get shortest angle difference (-180 to 180)
    private static float getShortestAngleDiff(float from, float to) {
        float diff = (to - from + 540) % 360 - 180;
        return diff;
    }

    public static void disable() {
        if (!enabled) return;
        enabled = false;

        if (!keyReset) {
            KeyBinding.setKeyPressed(client.options.forwardKey.getDefaultKey(), false);
            keyReset = true;
        }
    }

    public static String getBlockStatus() {
        return blockStatus;
    }
}
