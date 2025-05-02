package unblonded.packets.cheats;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
public class AirUnderCheck {

    private static final Minecraft mc = Minecraft.getInstance();
    public static String playerAirSafety = "";
    public static boolean isSafe = false;

    public static void checkSafety() {
        Player player = mc.player;
        if (player == null) return;

        BlockPos playerPos = player.blockPosition();

        BlockPos blockUnderPlayer = playerPos.below();
        BlockState blockStateUnderPlayer = player.level().getBlockState(blockUnderPlayer);

        BlockPos blockUnderBlockUnderPlayer = blockUnderPlayer.below();
        BlockState blockStateUnderBlockUnderPlayer = player.level().getBlockState(blockUnderBlockUnderPlayer);

        boolean notAir = blockStateUnderBlockUnderPlayer.getBlock() != Blocks.AIR &&
                         blockStateUnderBlockUnderPlayer.getBlock() != Blocks.LAVA;
        isSafe = !notAir;
        String state = notAir ? "Yes" : "No";

        playerAirSafety = ("Safe? -> "+ state + " | Y -> " + (int)player.getY());
    }
}