package unblonded.packets.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.component.Component;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.InjectorBridge;
import unblonded.packets.Packetedit;
import unblonded.packets.cfg;
import unblonded.packets.cheats.*;

import java.util.concurrent.ThreadLocalRandom;

import static unblonded.packets.cfg.writePlayerSaftey;

public class util {
    public static void inject(MinecraftClient client) {
        client.getWindow().setTitle("Packet Edit v3 - .inj");
        InjectorBridge.runExecutable("mcInject.exe");
        cfg.init();
        cfg.hasInjected = true;
    }

    public static void handleKeyInputs(MinecraftClient client) {
        if (Packetedit.isKeyDown(GLFW.GLFW_KEY_PERIOD) && client.currentScreen == null)
            client.setScreen(new ChatScreen("."));

        if (Keybinds.openGui.isPressed() && client.world != null)
            client.setScreen(new GuiBackground(Text.of("Packet Edit")));
    }

    public static void updateUI(MinecraftClient client) {
        if (cfg.safe) {
            cfg.readConfig();
            boolean worldLoaded = client.world != null;
            cfg.writeRenderFlag(client.currentScreen instanceof GuiBackground, worldLoaded);
        }
        if (cfg.displayplayers) cfg.writePlayerList(PlayerTracker.getNearbyPlayers());
        if (cfg.forwardTunnel) cfg.writeBlockStatus(ForwardTunnel.getBlockStatus());
    }


    public static void updateStates() {
        ForwardTunnel.setState(cfg.forwardTunnel);
        AutoCrystal.setState(cfg.autoCrystal);
        InteractionCanceler.setState(cfg.cancelInteraction);
        AutoAnchor.setState(cfg.autoAnchor);

        if (cfg.checkPlayerSafety) {
            AirUnderCheck.checkSafety();
            writePlayerSaftey(AirUnderCheck.playerAirSafety);
        }
    }
    public static void updateOreSim(MinecraftClient client) {
        if (client.world != null && client.player.age % 20 == 0) {
            OreSimulator.recalculateChunks();
        }
    }

    public static void setTitle(MinecraftClient client) {
        if (client.world != null) {
            String title = client.world.getRegistryKey().getValue().getPath() + " - Packet Edit v3 by Unblonded";
            client.getWindow().setTitle(title);
        } else {
            client.getWindow().setTitle("Packet Edit v3 by Unblonded");
        }
    }

    public static int rndInt(int max) {
        return ThreadLocalRandom.current().nextInt(-max, max + 1);
    }
}
