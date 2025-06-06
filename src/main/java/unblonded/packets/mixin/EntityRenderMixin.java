package unblonded.packets.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import unblonded.packets.util.Color;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRenderMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @WrapWithCondition(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private boolean render$render(M instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color, S state, MatrixStack matrices, VertexConsumerProvider consumers, int i) {

        if (!(state instanceof PlayerEntityRenderState playerState)) return true;

        PlayerEntity localPlayer = MinecraftClient.getInstance().player;
        if (localPlayer != null && playerState.id == localPlayer.getId()) {
            return true;
        }

        // Method 1: Simple approach
        // renderSimpleGlow(instance, matrixStack, consumers, light, overlay, playerState);

        // Method 2: Custom render layer approach
        renderWithCustomLayer(instance, matrixStack, consumers, light, overlay, playerState);

        return false;
    }

    private void renderSimpleGlow(M model, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay, PlayerEntityRenderState playerState) {
        int glowColor = 0xFF0000FF; // Red
        Identifier skinTexture = playerState.skinTextures.texture();

        // Disable depth test and render
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        VertexConsumer glowConsumer = consumers.getBuffer(RenderLayer.getEntityTranslucent(skinTexture));
        model.render(matrices, glowConsumer, 15728880, overlay, glowColor);

        RenderSystem.enableDepthTest();
    }

    private void renderWithCustomLayer(M model, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay, PlayerEntityRenderState playerState) {
        Identifier skinTexture = playerState.skinTextures.texture();

        // Use the same vertex format as the original entity rendering
        RenderLayer wallhackLayer = RenderLayer.of(
                "player_wallhack",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, // Keep this for now
                VertexFormat.DrawMode.TRIANGLES,
                1536,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM)
                        .texture(new RenderPhase.Texture(skinTexture, TriState.FALSE, false))
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .writeMaskState(RenderPhase.COLOR_MASK)
                        .cull(RenderPhase.DISABLE_CULLING) // Add this
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP) // Add this
                        .build(false)
        );

        // Try using the same light value as the normal render
        VertexConsumer wallhackConsumer = consumers.getBuffer(wallhackLayer);
        int redColor = 0x80FF0000;
        model.render(matrices, wallhackConsumer, light, overlay, redColor); // Use 'light' instead of 15728880
    }
}