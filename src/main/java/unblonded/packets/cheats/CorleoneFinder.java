package unblonded.packets.cheats;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import unblonded.packets.imgui.Alert;
import unblonded.packets.util.Chat;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.*;

public class CorleoneFinder {
    public static volatile int[] scanRadius = {512};
    public static Runnable scanTask;
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final Queue<BlockPos> foundPatterns = new ConcurrentLinkedQueue<>();

    private static final String[] PATTERN = {
            "$$$",
            "$$$",
            "$$$",
            "$$$",
            "$$$",
            "$$$",
            "$$$",
            "$$$",
            "$$$",
            "$$$",
    };

    // Symbol to block mapping
    private static final Map<Character, Block> SYMBOL_MAP = new HashMap<>();
    static {
        SYMBOL_MAP.put('$', Blocks.CYAN_TERRACOTTA);
    }

    public static void onInitializeClient() {
        scanTask = CorleoneFinder::scanForPattern;
    }

    public static void scan() {
        foundPatterns.clear();
        Alert.info("Pattern Scanner", "Started scanning for block pattern!");
        new Thread(() -> scanTask.run()).start();
    }

    public static void scanForPattern() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        BlockPos playerPos = client.player.getBlockPos();
        int cores = Runtime.getRuntime().availableProcessors();
        int worldHeight = client.world.getHeight();
        int patternHeight = PATTERN.length;

        // Calculate scan bounds
        int minScanY = Math.max(client.world.getBottomY(), playerPos.getY() - scanRadius[0]);
        int maxScanY = Math.min(worldHeight - patternHeight, playerPos.getY() + scanRadius[0]);

        int sliceHeight = Math.max(1, (maxScanY - minScanY) / cores);

        for (int i = 0; i < cores; i++) {
            int sliceMinY = minScanY + (i * sliceHeight);
            int sliceMaxY = (i == cores - 1) ? maxScanY : Math.min(maxScanY, sliceMinY + sliceHeight - 1);
            int coreId = i;

            executor.submit(() -> scanSlice(playerPos, sliceMinY, sliceMaxY, coreId));
        }
    }

    private static void scanSlice(BlockPos center, int minY, int maxY, int coreId) {
        MinecraftClient client = MinecraftClient.getInstance();
        int localFound = 0;

        // Scan each position in the slice
        for (int x = center.getX() - scanRadius[0]; x <= center.getX() + scanRadius[0]; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = center.getZ() - scanRadius[0]; z <= center.getZ() + scanRadius[0]; z++) {
                    BlockPos startPos = new BlockPos(x, y, z);

                    // Check both orientations: X-Y wall and Z-Y wall
                    if (matchesPatternXY(client.world, startPos) || matchesPatternZY(client.world, startPos)) {
                        foundPatterns.add(startPos);
                        localFound++;
                    }
                }
            }
        }

        Chat.sendMessage(Chat.prefix + " Pattern Scanner Core " + coreId +
                " Done! Found " + localFound + " patterns. Total: " + foundPatterns.size());
    }

    /**
     * Check if the pattern matches starting at the given position (X-Y orientation)
     * Pattern extends along X-axis horizontally and Y-axis vertically
     */
    private static boolean matchesPatternXY(World world, BlockPos startPos) {
        if (PATTERN.length == 0) return false;

        int patternWidth = PATTERN[0].length();
        int patternHeight = PATTERN.length;

        // Check each layer of the pattern
        for (int y = 0; y < patternHeight; y++) {
            String layer = PATTERN[y];

            // Check each position in this layer
            for (int x = 0; x < layer.length(); x++) {
                char symbol = layer.charAt(x);
                BlockPos checkPos = startPos.add(x, y, 0); // X-Y plane, Z stays constant

                if (!matchesSymbol(world, checkPos, symbol)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if the pattern matches starting at the given position (Z-Y orientation)
     * Pattern extends along Z-axis horizontally and Y-axis vertically
     */
    private static boolean matchesPatternZY(World world, BlockPos startPos) {
        if (PATTERN.length == 0) return false;

        int patternWidth = PATTERN[0].length();
        int patternHeight = PATTERN.length;

        // Check each layer of the pattern
        for (int y = 0; y < patternHeight; y++) {
            String layer = PATTERN[y];

            // Check each position in this layer
            for (int z = 0; z < layer.length(); z++) {
                char symbol = layer.charAt(z);
                BlockPos checkPos = startPos.add(0, y, z); // Z-Y plane, X stays constant

                if (!matchesSymbol(world, checkPos, symbol)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if a block at position matches the pattern symbol
     */
    private static boolean matchesSymbol(World world, BlockPos pos, char symbol) {
        // Wildcard matches anything
        if (symbol == '-') {
            return true;
        }

        // Check if symbol has a mapped block
        Block expectedBlock = SYMBOL_MAP.get(symbol);
        if (expectedBlock == null) {
            // Unknown symbol - treat as wildcard or log error
            return true;
        }

        BlockState actualState = world.getBlockState(pos);
        return actualState.isOf(expectedBlock);
    }

    /**
     * Update the pattern at runtime
     */
    public static void setPattern(String[] newPattern) {
        // Validate pattern
        if (newPattern.length == 0) return;

        int expectedWidth = newPattern[0].length();
        for (String layer : newPattern) {
            if (layer.length() != expectedWidth) {
                Chat.sendMessage(Chat.prefix + " Error: All pattern layers must have the same width!");
                return;
            }
        }

        System.arraycopy(newPattern, 0, PATTERN, 0, Math.min(newPattern.length, PATTERN.length));
        Chat.sendMessage(Chat.prefix + " Pattern updated! New pattern: " + newPattern.length +
                " layers, " + expectedWidth + " blocks wide");
    }

    /**
     * Add a new symbol mapping
     */
    public static void addSymbolMapping(char symbol, Block block) {
        SYMBOL_MAP.put(symbol, block);
        Chat.sendMessage(Chat.prefix + " Added symbol mapping: '" + symbol + "' -> " + block.toString());
    }

    /**
     * Get pattern info for debugging
     */
    public static void printPatternInfo() {
        Chat.sendMessage(Chat.prefix + " Current pattern:");
        for (int i = 0; i < PATTERN.length; i++) {
            Chat.sendMessage("Layer " + i + ": " + PATTERN[i]);
        }
        Chat.sendMessage("Symbol mappings: " + SYMBOL_MAP.toString());
    }

    /**
     * Clear found patterns
     */
    public static void clearResults() {
        foundPatterns.clear();
        Chat.sendMessage(Chat.prefix + " Cleared pattern search results");
    }
}