package unblonded.packets.cheats;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class AirUnderCheck {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static String playerAirSafety = "";
    public static boolean isSafe = false;

    public static String checkSafety() {
        PlayerEntity player = mc.player;
        if (player == null) return null;

        BlockPos playerPos = player.getBlockPos();

        BlockPos blockUnderPlayer = playerPos.down();
        BlockState blockStateUnderPlayer = player.getWorld().getBlockState(blockUnderPlayer);

        BlockPos blockUnderBlockUnderPlayer = blockUnderPlayer.down();
        BlockState blockStateUnderBlockUnderPlayer = player.getWorld().getBlockState(blockUnderBlockUnderPlayer);

        boolean notAir = blockStateUnderBlockUnderPlayer.getBlock() != Blocks.AIR &&
                blockStateUnderBlockUnderPlayer.getBlock() != Blocks.LAVA;
        isSafe = !notAir;
        String state = notAir ? "Yes" : "No";

        playerAirSafety = (" | Y -> " + (int)player.getY());
        return state;
    }
}