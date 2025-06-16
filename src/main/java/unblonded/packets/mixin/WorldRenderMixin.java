package unblonded.packets.mixin;

import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cfg;

@Mixin(WorldRenderer.class)
public class WorldRenderMixin {
    @Inject(method = "method_62216", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WeatherRendering;renderPrecipitation(Lnet/minecraft/world/World;Lnet/minecraft/client/render/VertexConsumerProvider;IFLnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    void weather(Fog fog, float f, Vec3d vec3d, int i, float g, CallbackInfo ci) {
        if (cfg.noRenderStuff[4].get()) ci.cancel();
    }
}
