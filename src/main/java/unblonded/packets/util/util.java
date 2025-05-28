package unblonded.packets.util;

import com.google.gson.JsonArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minidev.json.JSONObject;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.InjectorBridge;
import unblonded.packets.Packetedit;
import unblonded.packets.cfg;
import unblonded.packets.cheats.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class util {
    private static Thread updateThread;
    private static volatile boolean running = false;

    public static void handleKeyInputs(MinecraftClient client) {
        if (Keybinds.openGui.wasPressed() && (client.currentScreen == null))
            client.setScreen(new GuiBackground(Text.of("Packet Edit")));
    }

    public static void updateStates() {
        AutoCrystal.setState(cfg.autoCrystal.get());
        InteractionCanceler.setState(cfg.cancelInteraction.get());
        AutoAnchor.setState(cfg.autoAnchor.get());
        AutoTotem.setState(cfg.autoTotem.get(), cfg.autoTotemDelay[0], cfg.autoTotemHumanity[0]);
        AutoSell.setState(cfg.triggerAutoSell, cfg.autoSellDelay[0], cfg.autoSellPrice, cfg.autoSellEndpoints);
        AutoDisconnect.setState(cfg.autoDcPrimed.get(), cfg.autoDcProximity.get());
        AimAssist.setState(cfg.aimAssistToggle.get());
        AimAssist.applySettings(cfg.aimAssistRange[0], cfg.aimAssistFov[0], cfg.aimAssistSmoothness[0], cfg.aimAssistMinSpeed[0], cfg.aimAssistMaxSpeed[0], cfg.aimAssistVisibility, cfg.aimAssistUpdateRate[0]);
        InventoryScanner.setState(cfg.storageScan, cfg.storageScanSearch, cfg.storageScanColor);
        CrystalSpam.setState(cfg.crystalSpam.get(), cfg.crystalSpamSearchRadius[0], cfg.crystalSpamBreakDelay[0]);
        SelfCrystal.setState(cfg.selfCrystal.get());

        if (cfg.oreSim.get()) {
            OreSimulator.setWorldSeed(cfg.oreSimSeed.get());
            OreSimulator.setHorizontalRadius(cfg.oreSimDistance[0]);
        }

        if (cfg.checkPlayerAirSafety.get()) AirUnderCheck.checkSafety();
    }

    public static void updateOreSim(MinecraftClient client) {
        if (client.world != null && client.player.age % 20 == 0) {
            OreSimulator.recalculateChunks();
        }
    }

    public static void setTitle(MinecraftClient client) {
        if (client.world != null) {
            String suffix = " - Packet Edit v3 by Unblonded";
            String title = client.world.getRegistryKey().getValue().getPath();
            String formattedTitle = Arrays.stream(title.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            client.getWindow().setTitle(formattedTitle + suffix);
        } else {
            client.getWindow().setTitle("Packet Edit v3 by Unblonded");
        }
    }

    private static byte getKey() {
        AtomicReference<Byte> keyRef = new AtomicReference<>((byte) 0);
        Packetedit.contactServer("https://api.packetedit.top/key", response -> {
            try {
                String hexKey = new JSONObject(Integer.parseInt(response)).getAsString("key");

                hexKey = hexKey.replace("0x", "").trim();
                byte keyValue = (byte) Integer.parseInt(hexKey, 16);

                keyRef.set(keyValue);
                return null;
            } catch (Exception ignored) { return null; }
        });
        return keyRef.get();
    }

    public static String encrypt(String input) {
        byte key = getKey();
        byte[] data = input.getBytes();
        for (int i = 0; i < data.length; i++) data[i] ^= key;

        return Base64.getEncoder().encodeToString(data);
    }

    public static String decrypt(String encryptedBase64) {
        byte key = getKey();
        byte[] data = Base64.getDecoder().decode(encryptedBase64);
        for (int i = 0; i < data.length; i++) data[i] ^= key;

        return new String(data);
    }

    public static int rndInt(int max) {
        return ThreadLocalRandom.current().nextInt(-max, max + 1);
    }

    public static void showHWIDPopup(String hwid) {
        try {
            String clipboardCmd = "powershell.exe -Command \"Set-Clipboard -Value '" + hwid.replace("'", "''") + "'\"";
            Runtime.getRuntime().exec(clipboardCmd);

            // Show popup via PowerShell
            String popupCmd = "powershell.exe -Command \"Add-Type -AssemblyName PresentationFramework; " +
                    "[System.Windows.MessageBox]::Show('Auth failed. HWID copied to clipboard.', 'Packet Edit')\"";
            Runtime.getRuntime().exec(popupCmd);
        } catch (Exception e) {
            System.err.println("Failed to show popup or copy HWID: " + e.getMessage());
        }
    }

    public static double signedRandom(double max) {
        return (Math.random() * max) * (Math.random() < 0.5 ? -1 : 1);
    }

    public static void crash() {
        int i = Integer.MAX_VALUE;
        long[][][][][] memory = new long[i][i][i][i][i]; //danger fucking crash
        crash();
    }
}
