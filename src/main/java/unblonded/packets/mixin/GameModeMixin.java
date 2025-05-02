package unblonded.packets.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import unblonded.packets.util.GameModeAccessor;

@Mixin(MultiPlayerGameMode.class)
public class GameModeMixin implements GameModeAccessor {

    @Shadow
    @Final
    @Mutable
    private BlockPos destroyBlockPos;

    @Shadow
    private float destroyProgress;

    @Override
    public BlockPos getDestroyBlockPos() {
        return destroyBlockPos;
    }

    @Override
    public float getDestroyProgress() {
        return destroyProgress;
    }
}
