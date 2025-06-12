package unblonded.packets.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cfg;
import unblonded.packets.cheats.SwordBlocking;

@Mixin(HeldItemRenderer.class)
public class HandRenderMixin {
    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void scale(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof ClientPlayerEntity) || !((ClientPlayerEntity) entity).isMainPlayer()) return;
        if (cfg.handRender.get()) {
            float s = cfg.handRenderScale[0];
            float x = cfg.handRenderXYZ[0][0];
            float y = cfg.handRenderXYZ[1][0];
            float z = cfg.handRenderXYZ[2][0];
            matrices.scale(s,s,s);
            matrices.translate(x,y,z);
        }
        if (cfg.handRenderSwordBlock.get() && SwordBlocking.isEntityBlocking((ClientPlayerEntity) entity) && stack.getItem() instanceof SwordItem) {
            matrices.translate(0,0.05,0);
            matrices.multiply(new Quaternionf().rotateX((float) Math.toRadians(75)));
            matrices.multiply(new Quaternionf().rotateY((float) Math.toRadians(165)));
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(-95)));
        }
    }
}