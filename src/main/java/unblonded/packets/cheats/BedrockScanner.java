package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.Packetedit;

public class BedrockScanner implements ClientModInitializer {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean keyPressed = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (client.player == null || client.world == null) return;

            if (Packetedit.isKeyDown(GLFW.GLFW_KEY_B) && Packetedit.isKeyDown(GLFW.GLFW_KEY_INSERT)) {
                if (!keyPressed) {
                    keyPressed = true;
                    scanForBedrock();
                }
            } else {
                keyPressed = false;
            }
        });
    }

    private void scanForBedrock() {
        BlockPos playerPos = client.player.getBlockPos();
        int radius = 100;

        int y = 4;
        int count = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = new BlockPos(playerPos.getX() + x, y, playerPos.getZ() + z);
                if (client.world.getBlockState(checkPos).isOf(Blocks.BEDROCK)) {
                    count++;
                    String coord = "(" + checkPos.getX() + ", " + y + ", " + checkPos.getZ() + ")";
                    client.player.sendMessage(Text.of("Bedrock at: " + coord), false);
                }
            }
        }

        client.player.sendMessage(Text.of("Scan complete. Found " + count + " bedrock blocks at Y=4."), false);
    }
}
