package unblonded.packets.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import unblonded.packets.cfg;


import unblonded.packets.util.BlockColor;
import unblonded.packets.util.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static unblonded.packets.cfg.*;

public class ESPOverlayRenderer implements ClientModInitializer {
    private List<BlockPos> cachedOffsets = new ArrayList<>();
    private int currentBatch = 0;
    private BlockPos lastSearchPos = null;
    private long lastSearchTime = 0;
    private final Set<BlockPos> foundPositions = ConcurrentHashMap.newKeySet();
    private final CopyOnWriteArrayList<BlockPos> renderBuffer = new CopyOnWriteArrayList<>();

    public static void drawEspPos(WorldRenderContext context, BlockPos pos, Color color) {
        ClientLevel world = context.world();
        if (world == null) return;

        Camera camera = context.camera();
        PoseStack poseStack = context.matrixStack();
        Vec3 cameraPos = camera.getPosition();

        poseStack.pushPose();
        try {
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z); // Apply camera offset

            AABB bb = new AABB(pos);
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();

            boolean blockBelow = isTargetBlock(world, pos.below());
            boolean blockAbove = isTargetBlock(world, pos.above());
            boolean blockNorth = isTargetBlock(world, pos.north());
            boolean blockSouth = isTargetBlock(world, pos.south());
            boolean blockWest = isTargetBlock(world, pos.west());
            boolean blockEast = isTargetBlock(world, pos.east());

            Tesselator tessellator = Tesselator.getInstance();
            RenderSystem.setShader(CoreShaders.POSITION_COLOR); // required now
            BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

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

            BufferUploader.drawWithShader(buffer.build());
        } finally {
            poseStack.popPose();
        }
    }

    private static boolean isTargetBlock(ClientLevel world, BlockPos pos) {
        ArrayList<Block> targetBlocks = new ArrayList<>(
                List.of(
                        Blocks.DEEPSLATE_DIAMOND_ORE,
                        Blocks.DIAMOND_ORE
                )
        );
        return !targetBlocks.contains(world.getBlockState(pos).getBlock());
    }

    public static void drawTracers(WorldRenderContext context, Vec3 targetPos, Color color) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || targetPos == null) return;

        Player player = client.player;
        Camera camera = context.camera();
        PoseStack poseStack = context.matrixStack();
        Vec3 cameraPos = camera.getPosition();
        float tickDelta = context.camera().getPartialTickTime();

        // Interpolate player position to account for partial ticks
        double eyeX = Mth.lerp(tickDelta, player.xo, player.getX());
        double eyeY = Mth.lerp(tickDelta, player.yo, player.getY()) + player.getEyeHeight();
        double eyeZ = Mth.lerp(tickDelta, player.zo, player.getZ());
        Vec3 eyePos = new Vec3(eyeX, eyeY, eyeZ);

        // Get the player's view direction (looking direction) and calculate the crosshair position
        Vec3 viewVector = player.getViewVector(tickDelta);
        Vec3 crosshairPos = eyePos.add(viewVector.multiply(10, 10, 10));  // Adjust the multiplier to suit your needs (e.g., 10 is a reasonable distance)

        // Start matrix stack transformations
        poseStack.pushPose();
        try {
            // Apply camera offset
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z); // WORKS GREAT JUST ADD A SETTING INSIDE OF THE IMGUI CONFIG FOR TRACERS OR NOT TRACERS

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();

            // Set up rendering properties
            Tesselator tessellator = Tesselator.getInstance();
            RenderSystem.setShader(CoreShaders.POSITION_COLOR);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.lineWidth(1.5f);

            // Start the buffer and begin drawing lines
            BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            // Draw line from player's eye position to crosshair
            drawLine(buffer, matrix,
                    (float) targetPos.x, (float) targetPos.y, (float) targetPos.z,
                    (float) crosshairPos.x, (float) crosshairPos.y, (float) crosshairPos.z,
                    color);

            // Finish drawing with the shader
            BufferUploader.drawWithShader(buffer.build());
        } finally {
            poseStack.popPose();
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
                    BlockPos neighbor = current.offset(direction.getUnitVec3i());
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

            Vec3 avgPos = Vec3.ZERO;
            for (BlockPos block : group) {
                avgPos = avgPos.add(block.getX(), block.getY(), block.getZ());
            }
            avgPos = avgPos.multiply(1.0 / group.size(), 1.0 / group.size(), 1.0 / group.size());

            drawTracers(context, avgPos.add(0.5, 0.5, 0.5), color);
        }
    }

    private static void drawLine(BufferBuilder buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        buffer.addVertex(matrix, (float)x1, (float)y1, (float)z1).setColor(color.R(), color.G(), color.B(), color.A());
        buffer.addVertex(matrix, (float)x2, (float)y2, (float)z2).setColor(color.R(), color.G(), color.B(), color.A());
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null || !drawBlocks) return;  // Skip if drawBlocks is false

            BlockPos currentPos = client.player.blockPosition();
            updateOffsetsIfNeeded(RADIUS);
            if (shouldStartNewSearch(currentPos)) {
                startNewSearch(currentPos);
            }

            processSearchBatch(client, currentPos);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!advancedEsp) return;
            if (cfg.drawBlocks) try {
                Map<Color, List<BlockPos>> colorGroups = new HashMap<>();

                try {
                    for (BlockPos pos : renderBuffer) {
                            Block block = context.world().getBlockState(pos).getBlock();
                            Color color = getBlockColor(block);

                            ESPOverlayRenderer.drawEspPos(context, pos, color);
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
                currentPos.distSqr(lastSearchPos) > (RADIUS/2) * (RADIUS/2);
    }

    private void startNewSearch(BlockPos currentPos) {
        lastSearchPos = currentPos.immutable();
        lastSearchTime = System.currentTimeMillis();
        currentBatch = 0;
        foundPositions.clear();
    }

    private void processSearchBatch(Minecraft client, BlockPos currentPos) {
        if (currentBatch >= cachedOffsets.size()) return;

        int endIndex = Math.min(currentBatch + BATCH_SIZE, cachedOffsets.size());
        ClientLevel world = client.level;

        for (int i = currentBatch; i < endIndex; i++) {
            BlockPos offset = cachedOffsets.get(i);
            BlockPos targetPos = currentPos.offset(offset.getX(), offset.getY(), offset.getZ());

            // Skip unloaded chunks (reduces lag)
            if (!world.hasChunkAt(targetPos)) continue;

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