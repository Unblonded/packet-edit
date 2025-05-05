package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import unblonded.packets.util.GameModeAccessor;

public class ForwardTunnel implements ClientModInitializer {

    private static final Minecraft client = Minecraft.getInstance();
    private static boolean enabled = true;
    private static boolean keyReset = false;
    private static float targetYaw = -1f;
    private static final float SMOOTHING_SPEED = 1.0f;
    private static String blockStatus = "";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!enabled || client.player == null || client.level == null) return;

            Player player = client.player;
            if (!player.isAlive() || player.isSpectator()) return;

            setHeadPos();
            updateHeadRotation();

            Vec3 start = player.getEyePosition(1.0f);
            Vec3 look = player.getViewVector(1.0f).normalize();
            Vec3 end = start.add(look.scale(5.0));

            BlockHitResult hit = client.level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            GameModeAccessor accessor = (GameModeAccessor)client.gameMode;
            BlockPos pos = accessor.getDestroyBlockPos();
            float progress = accessor.getDestroyProgress();

            if (client.level.getBlockState(pos).getBlock() != Blocks.AIR)
                blockStatus = "Mining -> " + client.level.getBlockState(pos).getBlock().getName().getString() + " | " + Float.parseFloat(String.format("%.0f", progress*100)) + "%";

            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = hit.getBlockPos();

                if (!isTunnelPathSafe(player, targetPos, look)) {
                    disable();
                    return;
                }

                client.options.keyUp.setDown(true); // Start moving forward
                client.gameMode.continueDestroyBlock(targetPos, hit.getDirection());
                player.swing(InteractionHand.MAIN_HAND);
            }
        });
    }

    /**
     * Checks if the block ahead and its surroundings are safe for mining and walking into.
     */
    private boolean isTunnelPathSafe(Player player, BlockPos target, Vec3 lookVec) {
        // Blocks directly in the path
        for (int i = 0; i <= 2; i++) {
            Vec3 offset = lookVec.scale(i + 1);
            BlockPos ahead = target.offset((int) offset.x, (int) offset.y, (int) offset.z);

            if (isDangerousBlock(ahead)) return false;

            // Ensure the block below is not air or liquid
            BlockPos below = ahead.below();
            BlockState belowState = client.level.getBlockState(below);
            if (belowState.isAir() || isDangerousBlock(below)) return false;
        }

        // Prevent falling into holes
        BlockPos afterBreak = target.relative(player.getDirection());
        BlockPos belowAfterBreak = afterBreak.below();
        BlockState stateBelow = client.level.getBlockState(belowAfterBreak);
        if (stateBelow.isAir() || isDangerousBlock(belowAfterBreak)) return false;

        return true;
    }

    /**
     * Determines if the block at a given position is dangerous.
     */
    private boolean isDangerousBlock(BlockPos pos) {
        BlockState state = client.level.getBlockState(pos);
        return state.isAir()
                || state.is(Blocks.LAVA)
                || state.is(Blocks.WATER)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.VOID_AIR);
    }

    public static void setState(boolean state) {
        if (state) keyReset = false;
        enabled = state;
        if (!state) disable();
    }

    public static void updateHeadRotation() {
        if (client.player == null) return;

        if (targetYaw == -1f) return; // No target set

        float currentYaw = normalizeYaw(client.player.getYRot());

        float delta = getShortestAngleDiff(currentYaw, targetYaw);

        // Stop rotating if close enough
        if (Math.abs(delta) < 1.0f) {
            client.player.setYRot(targetYaw);
            client.player.setYHeadRot(targetYaw);
            targetYaw = -1f;
            return;
        }

        // Interpolate yaw
        float newYaw = normalizeYaw(currentYaw + Math.signum(delta) * Math.min(SMOOTHING_SPEED, Math.abs(delta)));
        client.player.setYRot(newYaw);
        client.player.setYHeadRot(newYaw);
    }

    public static void setHeadPos() {
        if (client.player == null) return;

        float yaw = normalizeYaw(client.player.getYRot());

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
            client.options.keyUp.setDown(false);
            keyReset = true;
        }
    }

    public static String getBlockStatus() {
        return blockStatus;
    }
}
