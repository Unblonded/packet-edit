package unblonded.packets.render;

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.util.thread.ThreadExecutor;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import unblonded.packets.util.Color;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class PlayerSee {

    static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            renderPlayerOverlays(context);
        });
    }

    private static void renderPlayerOverlays(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue; // Skip self

            Vec3d playerPos = player.getPos();
            double distance = cameraPos.distanceTo(playerPos);

            // Only render if player is within reasonable distance
            if (distance > 100) continue;

            // Create smooth glow effect
            renderPlayerGlow(context, player, getPlayerColor(player));
        }
    }

    private static void renderPlayerGlow(WorldRenderContext context, PlayerEntity player, Color color) {
        if (player == null || color == null) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = camera.getPos();
        Vec3d playerPos = player.getPos();

        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        try {
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            // Create bounding box that wraps around player with minimal expansion
            float expansion = 0.05f; // Very small expansion to wrap around player
            Box bb = new Box(
                    playerPos.x - player.getWidth()/2 - expansion,
                    playerPos.y - expansion,
                    playerPos.z - player.getWidth()/2 - expansion,
                    playerPos.x + player.getWidth()/2 + expansion,
                    playerPos.y + player.getHeight() + expansion,
                    playerPos.z + player.getWidth()/2 + expansion
            );

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            float r = color.R();
            float g = color.G();
            float b = color.B();
            float a = color.A() * getPlayerVisibilityAlpha(player);

            // Bottom face (Y-)
            buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a);

            // Top face (Y+)
            buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a);

            // North face (Z-)
            buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a);

            // South face (Z+)
            buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a);

            // West face (X-)
            buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a);

            // East face (X+)
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a);
            buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a);

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            matrices.pop();
        }
    }

    private static Color getPlayerColor(PlayerEntity player) {
        // You can customize colors based on player properties
        // For example, different colors for different teams, friends, etc.
        return new Color(0.3f, 0.8f, 1.0f, 0.4f); // Cyan glow
    }

    private static float getPlayerVisibilityAlpha(PlayerEntity player) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 1.0f;

        // Fade based on distance
        double distance = mc.player.getPos().distanceTo(player.getPos());
        float distanceAlpha = Math.max(0.1f, 1.0f - (float)(distance / 50.0));

        // Fade if player is sneaking
        float sneakAlpha = player.isSneaking() ? 0.3f : 1.0f;

        return distanceAlpha * sneakAlpha;
    }
}