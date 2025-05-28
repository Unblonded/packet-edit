package unblonded.packets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import unblonded.packets.util.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class PreLaunch implements PreLaunchEntrypoint {
    private static final String GITHUB_API = "https://api.github.com/repos/Unblonded/PacketEditDownloads/releases/latest";

    @Override
    public void onPreLaunch() {
        new Thread(() -> {
            try {
                String latest = fetchLatestVersion();
                if (isOutdated(getCurrentVersion(), latest)) {
                    System.out.println("[PacketEdit] New version " + latest + " available. Updating...");
                    System.exit(0);
                } else {
                    System.out.println("[PacketEdit] Already up to date.");
                }
            } catch (Exception e) {
                System.err.println("[PacketEdit] Failed to check for updates: " + e.getMessage());
            }
        }).start();
    }

    private String fetchLatestVersion() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(GITHUB_API).openConnection();
        conn.setRequestProperty("User-Agent", "PacketEdit-Updater");
        try (InputStream is = conn.getInputStream(); InputStreamReader reader = new InputStreamReader(is)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.get("tag_name").getAsString().replace("v", "");
        }
    }

    private static final String MOD_NAME_PREFIX = "packet-edit-";
    private static final String MOD_EXTENSION = ".jar";

    private static String getCurrentVersion() {
        File workDir = MinecraftClient.getInstance().runDirectory;
        File MOD_DIRECTORY = new File(workDir, "mods");
        File[] files = MOD_DIRECTORY.listFiles((dir, name) ->
                name.startsWith(MOD_NAME_PREFIX) && name.endsWith(MOD_EXTENSION));

        if (files == null || files.length == 0) {
            System.out.println("[PacketEdit] No mod files found in mods folder.");
            return "0.0.0"; // fallback
        }

        // Example: packet-edit-1.2.3.jar → 1.2.3
        String fileName = files[0].getName();
        String version = fileName
                .substring(MOD_NAME_PREFIX.length(), fileName.length() - MOD_EXTENSION.length());

        System.out.println("[PacketEdit] Detected current version: " + version);
        return version;
    }


    private boolean isOutdated(String current, String latest) {
        return !current.equals(latest);
    }
}
