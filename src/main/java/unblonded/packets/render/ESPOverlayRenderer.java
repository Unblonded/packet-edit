package unblonded.packets.render;

import com.mojang.blaze3d.platform.GlStateManager;
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
import unblonded.packets.util.PosColor;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ESPOverlayRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static List<BlockPos> cachedOffsets = new ArrayList<>();
    private static BlockPos lastSearchPos = null;
    private static long lastSearchTime = 0;
    private static final List<BlockPos> foundBlocks = new ArrayList<>();
    private static int lastRadius = -1;

    private static final ExecutorService scanExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ESP-Scanner");
        t.setDaemon(true);
        return t;
    });
    private static volatile boolean isScanning = false;
    private static final List<BlockPos> scanResults = Collections.synchronizedList(new ArrayList<>());

    public static void drawGlowPos(WorldRenderContext context, BlockPos pos, Color color) {
        World world = context.world();
        if (world == null || color == null) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = camera.getPos();

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

            Box bb = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            float a = color.A();
            float r = color.R();
            float g = color.G();
            float b = color.B();

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
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            Box bb = new Box(pos);
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            boolean blockAbove = isTargetBlock(world, pos.up());
            boolean blockBelow = isTargetBlock(world, pos.down());
            boolean blockNorth = isTargetBlock(world, pos.north());
            boolean blockSouth = isTargetBlock(world, pos.south());
            boolean blockWest = isTargetBlock(world, pos.west());
            boolean blockEast = isTargetBlock(world, pos.east());

            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            // Bottom face edges (Y-)
            if (!blockBelow) {
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.minZ, color); // North edge
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.minY, bb.maxZ, color); // South edge
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.minY, bb.maxZ, color); // West edge
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, color); // East edge
            }

            // Top face edges (Y+)
            if (!blockAbove) {
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, color); // North edge
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, color); // South edge
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.minX, bb.maxY, bb.maxZ, color); // West edge
                drawLine(buffer, matrix, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color); // East edge
            }

            // North face edges (Z-)
            if (!blockNorth) {
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.minZ, color); // Bottom edge
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, color); // Top edge
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.minZ, color); // Left edge
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, color); // Right edge
            }

            // South face edges (Z+)
            if (!blockSouth) {
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.maxX, bb.minY, bb.maxZ, color); // Bottom edge
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, color); // Top edge
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, color); // Left edge
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, color); // Right edge
            }

            // West face edges (X-)
            if (!blockWest) {
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.minY, bb.maxZ, color); // Bottom edge
                drawLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.minX, bb.maxY, bb.maxZ, color); // Top edge
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.minZ, color); // North edge
                drawLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, color); // South edge
            }

            // East face edges (X+)
            if (!blockEast) {
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, color); // Bottom edge
                drawLine(buffer, matrix, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color); // Top edge
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, color); // North edge
                drawLine(buffer, matrix, bb.maxX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, color); // South edge
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.lineWidth(1.0f);
            matrices.pop();
        }
    }

    private static boolean isTargetBlock(World world, BlockPos pos) {
        for (BlockColor blockColor : cfg.espBlockList) {
            if (blockColor.getBlock().equals(world.getBlockState(pos).getBlock())) {
                return true;
            }
        }
        return false;
    }

    public static void drawTracers(WorldRenderContext context, Vec3d targetPos, Color color) {
        if (mc.player == null || targetPos == null || color == null) return;

        PlayerEntity player = mc.player;
        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = camera.getPos();
        float tickDelta = context.camera().getLastTickDelta();

        double eyeX = MathHelper.lerp(tickDelta, player.prevX, player.getX());
        double eyeY = MathHelper.lerp(tickDelta, player.prevY, player.getY()) + player.getEyeHeight(player.getPose());
        double eyeZ = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
        Vec3d eyePos = new Vec3d(eyeX, eyeY, eyeZ);

        Vec3d viewVector = player.getRotationVec(tickDelta);
        Vec3d crosshairPos = eyePos.add(viewVector.multiply(10, 10, 10));

        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(1.5f);

        try {
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            drawLine(buffer, matrix,
                    (float) targetPos.x, (float) targetPos.y, (float) targetPos.z,
                    (float) crosshairPos.x, (float) crosshairPos.y, (float) crosshairPos.z,
                    color);

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
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
            if (client.player == null || client.world == null || !cfg.advEsp.get() || cfg.espBlockList.isEmpty()) {
                synchronized (foundBlocks) {
                    foundBlocks.clear();
                }
                lastSearchPos = null;
                return;
            }

            BlockPos currentPos = client.player.getBlockPos();
            updateOffsetsIfNeeded(cfg.espRadius[0]);

            if (shouldStartNewSearch(currentPos) && !isScanning) {
                isScanning = true;
                scanExecutor.submit(() -> performThreadedScan(client, currentPos));
                lastSearchPos = currentPos.toImmutable();
                lastSearchTime = System.currentTimeMillis();
            }

            if (!scanResults.isEmpty()) {
                synchronized (foundBlocks) {
                    foundBlocks.clear();
                    foundBlocks.addAll(scanResults);
                }
                scanResults.clear();
            }
        });


        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (cfg.drawBlocks.get()) {
                Map<Color, List<BlockPos>> colorGroups = new HashMap<>();

                for (BlockPos pos : foundBlocks) {
                    try {
                        if (context.world() == null) continue;
                        BlockState state = context.world().getBlockState(pos);
                        if (state == null || state.isAir()) continue;

                        Block block = state.getBlock();
                        Color color = getBlockColor(block);
                        if (color != null) {
                            if (cfg.advEspDrawType.get()) drawGlowPos(context, pos, color);
                            else drawEspPos(context, pos, color);
                            colorGroups.computeIfAbsent(color, k -> new ArrayList<>()).add(pos);
                        }
                    } catch (Exception e) {
                        System.err.println("Error rendering ESP for block: " + e.getMessage());
                    }
                }

                if (cfg.drawBlockTracer.get()) {
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

            if (cfg.oreSim.get()) {
                for (Map.Entry<Long, Set<PosColor>> entry : OreSimulator.chunkDebrisPositions.entrySet()) {
                    for (PosColor blockPos : entry.getValue()) {
                        try {
                            if (mc.world != null && mc.world.getBlockState(blockPos.pos).isOpaque()) {
                                if (cfg.oreSimDrawMode.get()) drawGlowPos(context, blockPos.pos, blockPos.color);
                                else drawEspPos(context, blockPos.pos, blockPos.color);
                            }
                        } catch (Exception e) {
                            System.err.println("Error rendering ore simulation: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    private static void performThreadedScan(MinecraftClient client, BlockPos searchCenter) {
        try {
            ClientWorld world = client.world;
            if (world == null) {
                isScanning = false;
                return;
            }

            List<BlockPos> results = new ArrayList<>();

            Set<Block> enabledBlocks = new HashSet<>();
            for (BlockColor blockColor : cfg.espBlockList) {
                if (blockColor.isEnabled() && blockColor.getBlock() != null) {
                    enabledBlocks.add(blockColor.getBlock());
                }
            }

            for (BlockPos offset : cachedOffsets) {
                BlockPos targetPos = searchCenter.add(offset);

                if (!world.isChunkLoaded(targetPos)) continue;

                try {
                    BlockState state = world.getBlockState(targetPos);
                    if (state.isAir()) continue;

                    Block block = state.getBlock();
                    if (enabledBlocks.contains(block)) {
                        results.add(targetPos);
                    }
                } catch (Exception e) { continue; }
            }
            scanResults.clear();
            scanResults.addAll(results);

        } catch (Exception e) {
            System.err.println("ESP scan error: " + e.getMessage());
        } finally {
            isScanning = false;
        }
    }

    private static Color getBlockColor(Block block) {
        if (block == null || cfg.espBlockList == null) return null;

        for (BlockColor blockColor : cfg.espBlockList) {
            if (!blockColor.isEnabled()) continue;
            if (blockColor.getBlock() != null && blockColor.getBlock().equals(block)) {
                return blockColor.getColor();
            }
        }
        return null;
    }

    private static void initializeOffsets(int radius) {
        Thread offsetThread = new Thread(() -> {
            List<BlockPos> tempOffsets = new ArrayList<>();
            int radiusSq = radius * radius;
            int minY = Math.max(-radius, -64);
            int maxY = Math.min(radius, 320);

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = minY; dy <= maxY; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx*dx + dy*dy + dz*dz <= radiusSq) {
                            tempOffsets.add(new BlockPos(dx, dy, dz));
                        }
                    }
                }
            }

            tempOffsets.sort(Comparator.comparingInt(pos -> pos.getX() * pos.getX() + pos.getY() * pos.getY() + pos.getZ() * pos.getZ()));

            synchronized (cachedOffsets) {
                cachedOffsets.clear();
                cachedOffsets.addAll(tempOffsets);
            }
        }, "OffsetCalculationThread");

        offsetThread.setDaemon(true);
        offsetThread.start();
    }

    private static boolean shouldStartNewSearch(BlockPos currentPos) {
        if (lastSearchPos == null) return true;
        long timeSinceLast = System.currentTimeMillis() - lastSearchTime;
        return timeSinceLast > cfg.espSearchTime[0] ||
                currentPos.getSquaredDistance(lastSearchPos) > ((double) cfg.espRadius[0] / 2) * ((double) cfg.espRadius[0] / 2);
    }

    private static void updateOffsetsIfNeeded(int currentRadius) {
        if (currentRadius != lastRadius) {
            initializeOffsets(currentRadius);
            lastRadius = currentRadius;
        }
    }
}