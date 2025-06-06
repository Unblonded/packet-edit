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

        renderWallhack(instance, matrixStack, consumers, light, overlay, playerState);
        return false;
    }

    private void renderWallhack(M model, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay, PlayerEntityRenderState playerState) {
        Identifier skinTexture = playerState.skinTextures.texture();

        // Create wallhack render layer that ALWAYS passes depth test
        RenderLayer wallhackLayer = RenderLayer.of(
                "player_wallhack",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                1536,
                true, // hasCrumbling
                true, // translucent
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM)
                        .texture(new RenderPhase.Texture(skinTexture, TriState.FALSE, false))
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .writeMaskState(RenderPhase.COLOR_MASK)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .build(false)
        );

        // Render wallhack version (always visible)
        VertexConsumer wallhackConsumer = consumers.getBuffer(wallhackLayer);
        int redColor = Color.WHITE.asHex();
        model.render(matrices, wallhackConsumer, light, overlay, redColor);

        // Render normal version (respects depth)
        VertexConsumer normalConsumer = consumers.getBuffer(RenderLayer.getEntityTranslucent(skinTexture));
        int whiteColor = Color.WHITE.asHex();
        model.render(matrices, normalConsumer, light, overlay, whiteColor);
    }
}