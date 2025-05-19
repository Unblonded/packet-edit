package unblonded.packets.util;

import com.google.gson.JsonArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
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

    public static void inject(MinecraftClient client) {
        client.getWindow().setTitle("Packet Edit v3 - .inj");
        cfg.init();

        while (!cfg.isReady) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.load(InjectorBridge.dllPath());
        cfg.hasInjected = true;
    }

    public static void handleKeyInputs(MinecraftClient client) {
        if (Packetedit.isKeyDown(GLFW.GLFW_KEY_PERIOD) && client.currentScreen == null)
            client.setScreen(new ChatScreen("."));

        if (Keybinds.openGui.wasPressed() && client.world != null && client.currentScreen == null)
            client.setScreen(new GuiBackground(Text.of("Packet Edit")));
    }

    public static void updateUI(MinecraftClient client) {
        if (!cfg.safe) return;

        cfg.readConfig();

        boolean worldLoaded = client.world != null;
        boolean shouldRender = client.currentScreen instanceof GuiBackground;
        boolean guiStorageScanner = cfg.storageScan && client.currentScreen instanceof HandledScreen<?>;
        boolean crosshairDraw = client.currentScreen == null || client.currentScreen instanceof GuiBackground;

        List<PlayerTracker.PlayerInfo> players = cfg.displayplayers ? PlayerTracker.getNearbyPlayers() : List.of();
        String playerSafety = cfg.checkPlayerSafety ? AirUnderCheck.checkSafety() : "";
        String blockStatus = cfg.forwardTunnel ? ForwardTunnel.getBlockStatus() : "";

        cfg.sendCombinedStatus(
                shouldRender,
                worldLoaded,
                players,
                playerSafety,
                blockStatus,
                guiStorageScanner,
                crosshairDraw
        );
    }

    public static void updateStates() {
        ForwardTunnel.setState(cfg.forwardTunnel);
        AutoCrystal.setState(cfg.autoCrystal);
        InteractionCanceler.setState(cfg.cancelInteraction);
        AutoAnchor.setState(cfg.autoAnchor);
        AutoTotem.setState(cfg.autoTotem, cfg.autoTotemDelay, cfg.autoTotemHumanity);
        AutoSell.setState(cfg.triggerAutoSell, cfg.autoSellDelay, cfg.autoSellPrice, cfg.autoSellEndpoints);
        AutoDisconnect.setState(cfg.autoDcPrimed, cfg.autoDcProximity);
        AimAssist.setState(cfg.aimAssistToggle);
        AimAssist.applySettings(cfg.aimAssistRange, cfg.aimAssistFov, cfg.aimAssistSmoothness, cfg.aimAssistMinSpeed, cfg.aimAssistMaxSpeed, cfg.aimAssistVisibility, cfg.aimAssistUpdateRate);
        InventoryScanner.setState(cfg.storageScan, cfg.storageScanSearch, cfg.storageScanColor);
        CrystalSpam.setState(cfg.crystalSpam, cfg.crystalSpamSearchRadius, cfg.crystalSpamBreakDelay);
    }

    public static void startUpdateThread(MinecraftClient client) {
        if (updateThread != null && updateThread.isAlive()) return; // already running

        running = true;
        updateThread = new Thread(() -> {
            while (running) {
                updateUI(client);
                updateStates();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Util-Update-Thread");
        updateThread.setDaemon(true);
        updateThread.start();
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
            client.getWindow().setTitle(formattedTitle+suffix);
        } else {
            client.getWindow().setTitle("Packet Edit v3 by Unblonded");
        }
    }

    private static byte getKey() {
        AtomicReference<Byte> keyRef = new AtomicReference<>((byte) 0);
        Packetedit.contactServer("https://api.packetedit.top/key", response -> {
            try {
                String hexKey = new JSONObject(Integer.parseInt(response)).getAsString("key");

                // Convert hex string to byte (remove "0x" prefix if present and parse as hex)
                hexKey = hexKey.replace("0x", "").trim();
                byte keyValue = (byte) Integer.parseInt(hexKey, 16);

                // Store the parsed key
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
            // Copy to clipboard via PowerShell
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

    public static Color colorFromJson(JsonArray arr) {
        return new Color(
                arr.get(0).getAsFloat(),
                arr.get(1).getAsFloat(),
                arr.get(2).getAsFloat(),
                arr.get(3).getAsFloat()
        );
    }

    public static double signedRandom(double max) {
        return (Math.random() * max) * (Math.random() < 0.5 ? -1 : 1);
    }



    public static void crash() {
        long[][][][][] memory = new long[Integer.MAX_VALUE][Integer.MAX_VALUE][Integer.MAX_VALUE][Integer.MAX_VALUE][Integer.MAX_VALUE]; //danger fucking crash
        crash();
    }
}
