package unblonded.packets.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
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

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static unblonded.packets.cfg.*;

public class ESPOverlayRenderer implements ClientModInitializer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private List<BlockPos> cachedOffsets = new ArrayList<>();
    private int currentBatch = 0;
    private BlockPos lastSearchPos = null;
    private long lastSearchTime = 0;
    private final Set<BlockPos> foundPositions = ConcurrentHashMap.newKeySet();
    private final CopyOnWriteArrayList<BlockPos> renderBuffer = new CopyOnWriteArrayList<>();

    public static void drawEspPos(WorldRenderContext context, BlockPos pos, Color color) {
        World world = context.world();
        if (world == null) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = camera.getPos();

        matrices.push();
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
        if (mc.player == null || targetPos == null) return;

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
        try {
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z); // WORKS GREAT JUST ADD A SETTING INSIDE OF THE IMGUI CONFIG FOR TRACERS OR NOT TRACERS

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            // Set up rendering properties
            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.lineWidth(1.5f);

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
            matrices.pop();
        }
    }

    public static void drawGroupedTracers(WorldRenderContext context, List<BlockPos> blocks, Color color) {
        if (blocks.isEmpty()) return;

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

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || !drawBlocks || !oreSim) return;

            BlockPos currentPos = client.player.getBlockPos();
            updateOffsetsIfNeeded(RADIUS);
            if (shouldStartNewSearch(currentPos)) {
                startNewSearch(currentPos);
            }

            processSearchBatch(client, currentPos);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (cfg.drawBlocks && advancedEsp) try {
                Map<Color, List<BlockPos>> colorGroups = new HashMap<>();

                try {
                    for (BlockPos pos : renderBuffer) {
                            Block block = context.world().getBlockState(pos).getBlock();
                            Color color = getBlockColor(block);

                            drawEspPos(context, pos, color);
                            colorGroups.computeIfAbsent(color, k -> new ArrayList<>()).add(pos);
                    }
                } catch (Exception ignored) {}

                if (cfg.drawBlockTracer) {
                    try {
                        for (Map.Entry<Color, List<BlockPos>> entry : colorGroups.entrySet())
                            drawGroupedTracers(context, entry.getValue(), entry.getKey());
                    } catch (Exception ignored) {}
                }

            } catch (Exception ignored) {}
            if (cfg.oreSim) {
                for (Map.Entry<Long, Set<Vec3d>> entry : OreSimulator.chunkDebrisPositions.entrySet()) {
                    for (Vec3d pos : entry.getValue()) {
                        BlockPos seedPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
                        if (mc.world.getBlockState(seedPos).isOpaque())
                            drawEspPos(context, seedPos, oreSimColor);
                    }
                }
            }
        });
    }

    private Color getBlockColor(Block block) {
        for (BlockColor blockColor : cfg.espBlockList) {
            if (blockColor.getBlock().equals(block)) {
                return blockColor.getColor();
            }
        }
        return null;
    }


    private int lastRadius = -1;

    private void updateOffsetsIfNeeded(int currentRadius) {
        if (currentRadius != lastRadius) {
            cachedOffsets.clear();
            initializeOffsets(currentRadius);
            lastRadius = currentRadius;
        }
    }

    private void initializeOffsets(int radius) {
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

    private boolean shouldStartNewSearch(BlockPos currentPos) {
        if (lastSearchPos == null) return true;
        long timeSinceLast = System.currentTimeMillis() - lastSearchTime;
        return timeSinceLast > SEARCH_INTERVAL ||
                currentPos.getSquaredDistance(lastSearchPos) > ((double) RADIUS /2) * ((double) RADIUS /2);
    }

    private void startNewSearch(BlockPos currentPos) {
        lastSearchPos = currentPos.toImmutable();
        lastSearchTime = System.currentTimeMillis();
        currentBatch = 0;
        foundPositions.clear();
    }

    private void processSearchBatch(MinecraftClient client, BlockPos currentPos) {
        if (currentBatch >= cachedOffsets.size()) return;

        int endIndex = Math.min(currentBatch + BATCH_SIZE, cachedOffsets.size());
        ClientWorld world = client.world;

        for (int i = currentBatch; i < endIndex; i++) {
            BlockPos offset = cachedOffsets.get(i);
            BlockPos targetPos = currentPos.add(offset.getX(), offset.getY(), offset.getZ());

            if (!world.isChunkLoaded(targetPos)) continue;

            for (BlockColor block : cfg.espBlockList) {
                if (block.getBlock().equals(world.getBlockState(targetPos).getBlock()) && !foundPositions.contains(targetPos)) {
                    foundPositions.add(targetPos);
                    break;
                }
            }
        }

        currentBatch = endIndex;

        if (currentBatch >= cachedOffsets.size()) {
            renderBuffer.clear();
            renderBuffer.addAll(foundPositions);
            foundPositions.clear();
        }
    }
}