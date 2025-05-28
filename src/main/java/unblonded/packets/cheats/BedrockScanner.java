package unblonded.packets.cheats;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.Packetedit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class BedrockScanner {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static boolean keyPressed = false;

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (client.player == null || client.world == null) return;

            if (Packetedit.isKeyDown(GLFW.GLFW_KEY_B) && Packetedit.isKeyDown(GLFW.GLFW_KEY_R) && Packetedit.isKeyDown(GLFW.GLFW_KEY_S)) {
                if (!keyPressed) {
                    keyPressed = true;
                    scanForBedrock();
                }
            } else {
                keyPressed = false;
            }
        });
    }

    private static void scanForBedrock() {
        BlockPos playerPos = client.player.getBlockPos();
        int radius = 100;
        int y = 4;

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = new BlockPos(playerPos.getX() + x, y, playerPos.getZ() + z);
                if (client.world.getBlockState(checkPos).isOf(Blocks.BEDROCK)) {
                    count++;
                    sb.append(checkPos.getX()).append(" ")
                            .append(y).append(" ")
                            .append(checkPos.getZ()).append(" Bedrock\n");
                }
            }
        }

        String result = sb.toString();
        if (!result.isEmpty()) {
            try {
                File file = bedrockPath();
                file.getParentFile().mkdirs();

                FileWriter writer = new FileWriter(file);
                writer.write(result);
                writer.close();

                client.player.sendMessage(Text.of("Saved " + count + " bedrock coordinates to " + file), false);
            } catch (IOException e) {
                client.player.sendMessage(Text.of("Failed to save file: " + e.getMessage()), false);
            }
        } else {
            client.player.sendMessage(Text.of("No bedrock found at Y=4 within radius."), false);
        }
    }

    public static File bedrockPath() {
        return new File(Packetedit.workDir(), "bedrock.txt");
    }
}