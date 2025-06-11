package unblonded.packets.cheats;

import unblonded.packets.cfg;
import unblonded.packets.imgui.Alert;
import unblonded.packets.util.Chat;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrottoFinder {
    public static volatile int[] radius = {512};
    public static Runnable scanTask;
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void onInitializeClient() {
        scanTask = GrottoFinder::scanForMagentaGlass;
    }

    public static void scan() {
        cfg.grottoFinderPositions.clear();
        Alert.info("Fairy Grotto Finder", "Started Scan For Fairy Grotto!");
        new Thread(() -> scanTask.run()).start();
    }

    public static void scanForMagentaGlass() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        BlockPos playerPos = client.player.getBlockPos();

        int cores = Runtime.getRuntime().availableProcessors();
        int height = client.world.getHeight();

        int sliceHeight = Math.max(1, height / cores);

        for (int i = 0; i < cores; i++) {
            int minY = i * sliceHeight;
            int maxY = Math.min(height - 1, (i + 1) * sliceHeight - 1);
            int core = i;

            executor.submit(() -> scanSlice(playerPos, minY, maxY, core));
        }
    }

    private static void scanSlice(BlockPos center, int minY, int maxY, int i) {
        MinecraftClient client = MinecraftClient.getInstance();
        for (int x = center.getX() - radius[0]; x <= center.getX() + radius[0]; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = center.getZ() - radius[0]; z <= center.getZ() + radius[0]; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (client.world.getBlockState(pos).isOf(Blocks.MAGENTA_STAINED_GLASS_PANE)) {
                        cfg.grottoFinderPositions.add(pos);
                    }
                }
            }
        }
        Chat.sendMessage(Chat.prefix+" Grotto Finder Core " +i + " Done! Total Found-> " + cfg.grottoFinderPositions.size());
    }
}
