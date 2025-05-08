package unblonded.packets.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import unblonded.packets.cfg;
import unblonded.packets.cheats.AirUnderCheck;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinBlockBreaking {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null) return;
        BlockPos playerPos = client.player.getBlockPos().down();
        if (AirUnderCheck.isSafe && pos.equals(playerPos) && cfg.checkPlayerSafety) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
