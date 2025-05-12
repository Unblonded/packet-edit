package unblonded.packets.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import unblonded.packets.cfg;

import unblonded.packets.cheats.OreSimulator;
import unblonded.packets.util.BlockColor;
import unblonded.packets.util.Color;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ESPOverlayRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static List<BlockPos> cachedOffsets = new ArrayList<>();
    private static int currentBatch = 0;
    private static BlockPos lastSearchPos = null;
    private static long lastSearchTime = 0;
    private static final Set<BlockPos> foundPositions = ConcurrentHashMap.newKeySet();
    private static final CopyOnWriteArrayList<BlockPos> renderBuffer = new CopyOnWriteArrayList<>();

    public static void drawEspPos(WorldRenderContext context, BlockPos pos, Color color) {
        World world = context.world();
        if (world == null || color == null) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = camera.getPos();

        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);

        try {
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z); // Apply camera offset

            Box bb = new Box(pos);
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            boolean blockBelow = isTargetBlock(world, pos.up());
            boolean blockAbove = isTargetBlock(world, pos.down());
            boolean blockNorth = isTargetBlock(world, pos.north());
            boolean blockSouth = isTargetBlock(world, pos.south());
            boolean blockWest = isTargetBlock(world, pos.west());
            boolean blockEast = isTargetBlock(world, pos.east());

            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            if (blockBelow && blockNorth)
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.minZ, color);
            if (blockBelow && blockSouth)
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.minY, bb.maxZ, color);
            if (blockBelow && blockWest)
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.minY, bb.maxZ, color);
            if (blockBelow && blockEast)
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, color);
            if (blockAbove && blockNorth)
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, color);
            if (blockAbove && blockSouth)
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, color);
            if (blockAbove && blockWest)
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.minX, bb.maxY, bb.maxZ, color);
            if (blockAbove && blockEast)
                drawLine(buffer, matrix, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color);
            if (blockNorth && blockWest)
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.minZ, color);
            if (blockNorth && blockEast)
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, color);
            if (blockSouth && blockWest)
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, color);
            if (blockSouth && blockEast)
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, color);

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            // Always restore render state regardless of what happens
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.lineWidth(1.0f);
            matrices.pop();
        }
    }

    private static boolean isTargetBlock(World world, BlockPos pos) {
        for (BlockColor blockColor : cfg.espBlockList) {
            if (blockColor.getBlock().equals(world.getBlockState(pos).getBlock())) {
                return false;
            }
        }
        return true;
    }

    public static void drawTracers(WorldRenderContext context, Vec3d targetPos, Color color) {
        if (mc.player == null || targetPos == null || color == null) return;

        PlayerEntity player = mc.player;
        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = camera.getPos();
        float tickDelta = context.camera().getLastTickDelta();

        // Interpolate player position to account for partial ticks
        double eyeX = MathHelper.lerp(tickDelta, player.prevX, player.getX());
        double eyeY = MathHelper.lerp(tickDelta, player.prevY, player.getY()) + player.getEyeHeight(player.getPose());
        double eyeZ = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
        Vec3d eyePos = new Vec3d(eyeX, eyeY, eyeZ);

        Vec3d viewVector = player.getRotationVec(tickDelta);
        Vec3d crosshairPos = eyePos.add(viewVector.multiply(10, 10, 10));

        // Start matrix stack transformations
        matrices.push();

        // Set up rendering properties
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);

        try {
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            // Set up rendering properties
            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            // Start the buffer and begin drawing lines
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            // Draw line from player's eye position to crosshair
            drawLine(buffer, matrix,
                    (float) targetPos.x, (float) targetPos.y, (float) targetPos.z,
                    (float) crosshairPos.x, (float) crosshairPos.y, (float) crosshairPos.z,
                    color);

            // Finish drawing with the shader
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            // Always restore render state
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.lineWidth(1.0f);
            matrices.pop();
        }
    }

    public static void drawGroupedTracers(WorldRenderContext context, List<BlockPos> blocks, Color color) {
        if (blocks.isEmpty() || color == null) return;

        List<List<BlockPos>> groups = new ArrayList<>();
        Set<BlockPos> processed = new HashSet<>();

        for (BlockPos block : blocks) {
            if (processed.contains(block)) continue;

            List<BlockPos> group = new ArrayList<>();
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(block);
            processed.add(block);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                group.add(current);

                // Check all 6 adjacent blocks
                for (Direction direction : Direction.values()) {
                    BlockPos neighbor = current.offset(direction);
                    if (blocks.contains(neighbor) && !processed.contains(neighbor)) {
                        processed.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            groups.add(group);
        }

        for (List<BlockPos> group : groups) {
            if (group.isEmpty()) continue;

            Vec3d avgPos = Vec3d.ZERO;
            for (BlockPos block : group) {
                avgPos = avgPos.add(block.getX(), block.getY(), block.getZ());
            }
            avgPos = avgPos.multiply(1.0 / group.size(), 1.0 / group.size(), 1.0 / group.size());

            drawTracers(context, avgPos.add(0.5, 0.5, 0.5), color);
        }
    }

    private static void drawLine(BufferBuilder buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(color.R(), color.G(), color.B(), color.A());
        buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(color.R(), color.G(), color.B(), color.A());
    }

    public static void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // Only run if ESP is enabled and conditions are met
            if (client.player == null || client.world == null || !cfg.advancedEsp || cfg.espBlockList.isEmpty()) {
                // Create a new buffer instance instead of just clearing
                synchronized (renderBuffer) {
                    renderBuffer.clear();
                    foundPositions.clear();
                    lastSearchPos = null; // Reset search state too
                }
                return;
            }

            BlockPos currentPos = client.player.getBlockPos();
            updateOffsetsIfNeeded(cfg.RADIUS);

            if (shouldStartNewSearch(currentPos)) {
                startNewSearch(currentPos);
            }

            processSearchBatch(client, currentPos);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (cfg.drawBlocks) {
                Map<Color, List<BlockPos>> colorGroups = new HashMap<>();

                // Use a local copy to avoid concurrent modification issues
                List<BlockPos> localCopy;
                synchronized (renderBuffer) {
                    localCopy = new ArrayList<>(renderBuffer);
                }

                for (BlockPos pos : localCopy) {
                    try {
                        if (context.world() == null) continue;
                        BlockState state = context.world().getBlockState(pos);
                        if (state == null) continue;

                        Block block = state.getBlock();
                        Color color = getBlockColor(block);
                        if (color != null) {
                            drawEspPos(context, pos, color);
                            colorGroups.computeIfAbsent(color, k -> new ArrayList<>()).add(pos);
                        }
                    } catch (Exception e) {
                        // Log exception but continue processing
                        System.err.println("Error rendering ESP for block: " + e.getMessage());
                    }
                }

                if (cfg.drawBlockTracer) {
                    try {
                        for (Map.Entry<Color, List<BlockPos>> entry : colorGroups.entrySet()) {
                            if (entry.getKey() != null && !entry.getValue().isEmpty()) {
                                drawGroupedTracers(context, entry.getValue(), entry.getKey());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error rendering tracers: " + e.getMessage());
                    }
                }
            }

            if (cfg.oreSim) {
                for (Map.Entry<Long, Set<Vec3d>> entry : OreSimulator.chunkDebrisPositions.entrySet()) {
                    for (Vec3d pos : entry.getValue()) {
                        try {
                            BlockPos seedPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
                            if (mc.world != null && mc.world.getBlockState(seedPos).isOpaque()) {
                                drawEspPos(context, seedPos, cfg.oreSimColor);
                            }
                        } catch (Exception e) {
                            System.err.println("Error rendering ore simulation: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    private static Color getBlockColor(Block block) {
        if (block == null || cfg.espBlockList == null) return null;

        for (BlockColor blockColor : cfg.espBlockList) {
            if (!blockColor.isEnabled()) continue; // Skip disabled blocks
            if (blockColor.getBlock() != null && blockColor.getBlock().equals(block)) {
                return blockColor.getColor();
            }
        }
        return null;
    }

    private static int lastRadius = -1;

    private static void initializeOffsets(int radius) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx*dx + dy*dy + dz*dz <= radiusSq) {
                        cachedOffsets.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
        }
    }

    private static boolean shouldStartNewSearch(BlockPos currentPos) {
        if (lastSearchPos == null) return true;
        long timeSinceLast = System.currentTimeMillis() - lastSearchTime;
        return timeSinceLast > cfg.SEARCH_INTERVAL ||
                currentPos.getSquaredDistance(lastSearchPos) > ((double) cfg.RADIUS /2) * ((double) cfg.RADIUS /2);
    }

    private static void startNewSearch(BlockPos currentPos) {
        lastSearchPos = currentPos.toImmutable();
        lastSearchTime = System.currentTimeMillis();
        currentBatch = 0;
        foundPositions.clear();
    }

    private static void processSearchBatch(MinecraftClient client, BlockPos currentPos) {
        if (currentBatch >= cachedOffsets.size()) return;

        int endIndex = Math.min(currentBatch + cfg.BATCH_SIZE, cachedOffsets.size());
        ClientWorld world = client.world;
        if (world == null) return;

        for (int i = currentBatch; i < endIndex; i++) {
            BlockPos offset = cachedOffsets.get(i);
            BlockPos targetPos = currentPos.add(offset);

            if (!world.isChunkLoaded(targetPos)) continue;

            BlockState state = world.getBlockState(targetPos);
            Block block = state.getBlock();

            // Check if block is in our ESP list
            for (BlockColor espBlock : cfg.espBlockList) {
                if (!espBlock.isEnabled()) continue;
                if (espBlock.getBlock().equals(block) && !foundPositions.contains(targetPos)) {
                    foundPositions.add(targetPos);
                    break; // No need to check other blocks once found
                }
            }
        }

        currentBatch = endIndex;

        // Update render buffer when search completes
        if (currentBatch >= cachedOffsets.size()) {
            synchronized (renderBuffer) {
                renderBuffer.clear();
                renderBuffer.addAll(foundPositions);
                foundPositions.clear();
            }
        }
    }

    // Other helper methods remain the same:
    private static void updateOffsetsIfNeeded(int currentRadius) {
        if (currentRadius != lastRadius) {
            cachedOffsets.clear();
            initializeOffsets(currentRadius);
            lastRadius = currentRadius;
        }
    }
}