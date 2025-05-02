package unblonded.packets.util;

import net.minecraft.core.BlockPos;

public interface GameModeAccessor {
    BlockPos getDestroyBlockPos();
    float getDestroyProgress();
}
