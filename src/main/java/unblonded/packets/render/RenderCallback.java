package unblonded.packets.render;

import unblonded.packets.cfg;
import unblonded.packets.cheats.CorleoneFinder;
import unblonded.packets.util.Color;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RenderCallback {
    public static void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            render(ctx, cfg.grottoFinderPositions, new Color(cfg.grottoFinderColor),
                    cfg.grottoFinderDrawMode, cfg.grottoFinderTracer.get());


            List<BlockPos> safePatterns;
            synchronized (CorleoneFinder.foundPatterns) {
                safePatterns = new ArrayList<>(CorleoneFinder.foundPatterns);
                render(ctx, safePatterns, new Color(cfg.corleoneFinderColor),
                        cfg.corleoneFinderDrawMode, cfg.corleoneFinderTracer.get());
            }
        });
    }

    private static void render(WorldRenderContext ctx, List<BlockPos> blocks, Color c, boolean mode, boolean drawTracers) {
        if (ctx.world() == null) return;
        List<BlockPos> validBlocks = blocks.stream().filter(pos -> isValidBlock(ctx, pos)).peek(pos -> renderBlock(ctx, pos, c, mode)).toList();
        if (drawTracers) ESP.drawGroupedTracers(ctx, validBlocks, c);
    }

    private static boolean isValidBlock(WorldRenderContext ctx, BlockPos pos) {
        try { return !ctx.world().getBlockState(pos).isAir(); }
        catch (Exception e) { return false; }
    }

    private static void renderBlock(WorldRenderContext ctx, BlockPos pos, Color c, boolean mode) {
        if (mode) ESP.drawGlowPos(ctx, pos, c);
        else ESP.drawEspPos(ctx, pos, c);
    }
}