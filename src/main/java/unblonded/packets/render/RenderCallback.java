package unblonded.packets.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import unblonded.packets.cfg;
import unblonded.packets.cheats.CorleoneFinder;
import unblonded.packets.cheats.Waypoints;
import unblonded.packets.util.Color;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RenderCallback {
    public static void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            try {
                render(ctx, cfg.grottoFinderPositions, new Color(cfg.grottoFinderColor),
                        cfg.grottoFinderDrawMode, cfg.grottoFinderTracer.get());


                List<BlockPos> safePatterns;
                synchronized (CorleoneFinder.foundPatterns) {
                    safePatterns = new ArrayList<>(CorleoneFinder.foundPatterns);
                    render(ctx, safePatterns, new Color(cfg.corleoneFinderColor),
                            cfg.corleoneFinderDrawMode, cfg.corleoneFinderTracer.get());
                }

                for (Waypoints.Point point : Waypoints.saved) {
                    render(ctx, List.of(point.pos), point.color, false, false);

                    Vec3d camPos = ctx.camera().getPos();
                    BlockPos pos = point.pos;
                    double dist = camPos.distanceTo(Vec3d.ofCenter(pos));

                    ctx.matrixStack().push();

                    // Calculate alpha based on distance
                    float alpha = (float) Math.min(1.0, Math.max(0.1, dist / 50.0));
                    int c = point.color.asHex();
                    int argb = ((int)(alpha * 255) << 24) | (c & 0x00FFFFFF);

                    // Translate to world position
                    ctx.matrixStack().translate(
                            pos.getX() - camPos.x,
                            pos.getY() - camPos.y,
                            pos.getZ() - camPos.z
                    );

                    // Render beacon beam
                    BeaconBlockEntityRenderer.renderBeam(
                            ctx.matrixStack(),
                            ctx.consumers(),
                            BeaconBlockEntityRenderer.BEAM_TEXTURE,
                            ctx.tickCounter().getTickDelta(false),
                            1.0f,
                            ctx.world().getTime(),
                            0,
                            ctx.world().getHeight(),
                            argb,
                            0.2f,
                            0.25f
                    );

                    ctx.matrixStack().translate(0.5, 1.5, 0.5);
                    ctx.matrixStack().push();
                    ctx.matrixStack().multiply(ctx.camera().getRotation());
                    float scale = 0.025f;
                    ctx.matrixStack().scale(scale, -scale, scale);

                    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                    String text = point.name != null ? point.name : "Waypoint"; // Use actual waypoint name

                    float textX = -textRenderer.getWidth(text) / 2f;
                    float textY = 0f;
                    textRenderer.draw(
                            text,
                            textX,
                            textY,
                            point.color.asHex() | 0xFF000000,
                            false,
                            ctx.matrixStack().peek().getPositionMatrix(),
                            ctx.consumers(),
                                TextRenderer.TextLayerType.SEE_THROUGH,
                            0,
                            15728880
                    );

                    ctx.matrixStack().pop();
                    ctx.matrixStack().pop();
                }
            } catch (Exception ignored) {}
        });
    }

    private static void render(WorldRenderContext ctx, List<BlockPos> blocks, Color c, boolean mode, boolean drawTracers) {
        if (ctx.world() == null) return;
        List<BlockPos> validBlocks = blocks.stream().filter(pos -> isValidBlock(ctx, pos)).peek(pos -> renderBlock(ctx, pos, c, mode)).toList();
        if (drawTracers) ESP.drawGroupedTracers(ctx, validBlocks, c);
    }

    private static boolean isValidBlock(WorldRenderContext ctx, BlockPos pos) {
        return true;
    }

    private static void renderBlock(WorldRenderContext ctx, BlockPos pos, Color c, boolean mode) {
        if (mode) ESP.drawGlowPos(ctx, pos, c);
        else ESP.drawEspPos(ctx, pos, c);
    }
}