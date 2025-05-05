package unblonded.packets.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.InjectorBridge;
import unblonded.packets.Packetedit;
import unblonded.packets.cfg;
import unblonded.packets.cheats.*;

import static unblonded.packets.cfg.writePlayerSaftey;

public class util {
    public static void inject(Minecraft client) {
        client.getWindow().setTitle("Packet Edit v3 - .inj");
        InjectorBridge.runExecutable("mcInject.exe");
        cfg.init();
        cfg.hasInjected = true;
    }

    public static void handleKeyInputs(Minecraft client) {
        if (Packetedit.isKeyDown(GLFW.GLFW_KEY_PERIOD) && client.screen == null)
            client.setScreen(new ChatScreen("."));

        if (Keybinds.openGui.consumeClick() && client.level != null)
            client.setScreen(new GuiBackground(Component.literal("Packet Edit")));
    }

    public static void updateUI(Minecraft client) {
        if (cfg.safe) {
            cfg.readConfig();
            boolean worldLoaded = client.level != null;
            cfg.writeRenderFlag(client.screen instanceof GuiBackground, worldLoaded);
        }
        if (cfg.displayplayers) cfg.writePlayerList(PlayerTracker.getNearbyPlayers());
        if (cfg.forwardTunnel) cfg.writeBlockStatus(ForwardTunnel.getBlockStatus());
    }


    public static void updateStates() {
        ForwardTunnel.setState(cfg.forwardTunnel);
        AutoCrystal.setState(cfg.autoCrystal);
        InteractionCanceler.setState(cfg.cancelInteraction);

        if (cfg.checkPlayerSafety) {
            AirUnderCheck.checkSafety();
            writePlayerSaftey(AirUnderCheck.playerAirSafety);
        }
    }
}
