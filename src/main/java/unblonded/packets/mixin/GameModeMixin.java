package unblonded.packets.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import unblonded.packets.util.GameModeAccessor;

@Mixin(ClientPlayerInteractionManager.class)
public class GameModeMixin implements GameModeAccessor {

    @Shadow
    @Mutable
    private BlockPos currentBreakingPos;

    @Shadow
    private float currentBreakingProgress;

    @Override
    public BlockPos getDestroyBlockPos() {
        return currentBreakingPos;
    }

    @Override
    public float getDestroyProgress() {
        return currentBreakingProgress;
    }
}
