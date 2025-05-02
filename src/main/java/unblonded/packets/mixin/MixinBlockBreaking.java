package unblonded.packets.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import unblonded.packets.cfg;
import unblonded.packets.cheats.AirUnderCheck;

@Mixin(MultiPlayerGameMode.class)
public class MixinBlockBreaking {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onStartDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player == null) return;
        BlockPos playerPos = minecraft.player.blockPosition().below();
        if (AirUnderCheck.isSafe && blockPos.equals(playerPos) && cfg.checkPlayerSafety) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
