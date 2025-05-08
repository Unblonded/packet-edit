package unblonded.packets.util;

import net.minecraft.util.math.BlockPos;

public interface GameModeAccessor {
    BlockPos getDestroyBlockPos();
    float getDestroyProgress();
}
