package unblonded.packets.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cfg;

@Mixin(ClientPlayNetworkHandler.class)
public class PacketReception {
    @Inject(method = "onWorldTimeUpdate", at = @At("HEAD"), cancellable = true)
    private void onTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        if (cfg.timeChanger.get()) ci.cancel();
    }
}
