package unblonded.packets;

import unblonded.packets.cheats.*;
import unblonded.packets.render.*;
import unblonded.packets.util.*;

import net.fabricmc.api.ClientModInitializer;
import unblonded.packets.util.CmdManager.Cmds.MemoryCommand;
import unblonded.packets.util.CmdManager.Cmds.GCCommand;
import unblonded.packets.util.CmdManager.CommandManager;

public class Initializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Packetedit.onInitializeClient();
        PlayerTracker.onInitializeClient();
        ESPOverlayRenderer.onInitializeClient();
        AutoCrystal.onInitializeClient();
        InteractionCanceler.onInitializeClient();
        Keybinds.onInitializeClient();
        BedrockScanner.onInitializeClient();
        AutoAnchor.onInitializeClient();
        AutoTotem.onInitializeClient();
        AutoSell.onInitializeClient();
        AutoDisconnect.onInitializeClient();
        AimAssist.onInitializeClient();
        SelfCrystal.onInitializeClient();

        CommandManager.register(new GCCommand());
        CommandManager.register(new MemoryCommand());
        CommandManager.init();
    }
}
