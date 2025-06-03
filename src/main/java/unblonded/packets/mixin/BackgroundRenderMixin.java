package unblonded.packets.mixin;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import unblonded.packets.cfg;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRenderMixin {
    @ModifyArgs(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Fog;<init>(FFLnet/minecraft/client/render/FogShape;FFFF)V"))
    private static void fog(Args args, Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta) {
        if (cfg.noRender.get() && cfg.noRenderItems[3].get() && fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            args.set(0, viewDistance * 10);
            args.set(1, viewDistance * 10);
        }
    }
}