package unblonded.packets;

import unblonded.packets.cheats.*;
import unblonded.packets.render.*;
import unblonded.packets.util.*;

import net.fabricmc.api.ClientModInitializer;

public class Initializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Packetedit.onInitializeClient();
        PlayerTracker.onInitializeClient();
        ESPOverlayRenderer.onInitializeClient();
        ForwardTunnel.onInitializeClient();
        AutoCrystal.onInitializeClient();
        InteractionCanceler.onInitializeClient();
        Keybinds.onInitializeClient();
        BedrockScanner.onInitializeClient();
        AutoAnchor.onInitializeClient();
        AutoTotem.onInitializeClient();
        AutoSell.onInitializeClient();
        AutoDisconnect.onInitializeClient();
        InventoryScanner.onInitializeClient();
        AimAssist.onInitializeClient();
    }
}
