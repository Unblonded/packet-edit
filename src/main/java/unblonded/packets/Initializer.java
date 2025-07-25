package unblonded.packets;

import unblonded.packets.cheats.*;
import unblonded.packets.render.*;
import unblonded.packets.util.*;

import unblonded.packets.util.CmdManager.Cmds.*;
import unblonded.packets.util.CmdManager.CommandManager;

import net.fabricmc.api.ClientModInitializer;

public class Initializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Packetedit.onInitializeClient();
        PlayerTracker.onInitializeClient();
        ESP.onInitializeClient();
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
        TotemNotifier.onInitializeClient();
        TimeChanger.onInitializeClient();
        AutoBreachSwap.onInitializeClient();
        GrottoFinder.onInitializeClient();
        RenderCallback.onInitializeClient();
        CorleoneFinder.onInitializeClient();

        CommandManager.register(new GCCommand());
        CommandManager.register(new MemoryCommand());
        CommandManager.register(new ToggleCommand());
        CommandManager.register(new GodSwordCommand());
        CommandManager.register(new AlertTest());
        CommandManager.register(new WaypointCommand());
        CommandManager.init();
    }
}
