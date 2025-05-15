package unblonded.packets;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import unblonded.packets.cheats.OreSimulator;
import unblonded.packets.util.BlockColor;
import unblonded.packets.util.Color;
import unblonded.packets.util.util;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class cfg {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    public static boolean safe = false;
    private static Thread connectionThread;
    private static int currentPort = 1337;
    private static final String CONFIG_FILENAME = "packetedit-port.json";
    public static boolean isReady = false;

    public static void init() {
        // Try to find an available port starting from 1337
        currentPort = findAvailablePort(1337);

        connectionThread = new Thread(() -> {
            while (socket == null && !Thread.currentThread().isInterrupted()) {
                try {
                    socket = new Socket("127.0.0.1", currentPort);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    safe = true;
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.getWindow().setTitle("Packet Edit v3 - Ready (Port: " + currentPort + ")");
                } catch (IOException e) {
                    try {
                        isReady = true;
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        connectionThread.start();
    }

    private static Path getConfigPath() throws IOException {
        Path minecraftDir = Paths.get(System.getenv("APPDATA"), ".minecraft");

        if (!Files.exists(minecraftDir)) {
            Files.createDirectories(minecraftDir);
        }

        return minecraftDir.resolve(CONFIG_FILENAME);
    }

    private static int findAvailablePort(int startingPort) {
        try {
            Path configPath = getConfigPath();

            // Read existing config if present
            if (Files.exists(configPath)) {
                try {
                    String content = Files.readString(configPath);
                    JsonObject json = new Gson().fromJson(content, JsonObject.class);
                    if (json != null && json.has("port")) {
                        int savedPort = json.get("port").getAsInt();
                        if (isPortAvailable(savedPort)) {
                            return savedPort;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error reading port config: " + e.getMessage());
                    try {
                        Files.delete(configPath);
                    } catch (IOException deleteEx) {
                        System.err.println("Couldn't delete invalid config: " + deleteEx.getMessage());
                    }
                }
            }

            // Find next available port
            int port = startingPort;
            while (port < 65535) {
                if (isPortAvailable(port)) {
                    // Port is available - save to config
                    try {
                        JsonObject json = new JsonObject();
                        json.addProperty("port", port);

                        Files.writeString(configPath, new Gson().toJson(json),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);

                        System.out.println("Found and saved available port: " + port);
                        return port;
                    } catch (IOException e) {
                        System.err.println("Error saving port config: " + e.getMessage());
                        // If we can't save the config, still return the port
                        return port;
                    }
                }
                port++;
            }

            System.err.println("No available ports found, using default");
            return startingPort;
        } catch (Exception e) {
            System.err.println("Critical error finding available port: " + e.getMessage());
            e.printStackTrace();
            return startingPort;
        }
    }

    /**
     * Checks if a specific port is available
     */
    private static boolean isPortAvailable(int port) {
        // Only check if we can bind to the port as a server
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Port is in use or not available
            return false;
        }
    }


    private static long lastOreSimSeed = -1;
    private static int lastOreSimDistance = -1;

    public static boolean hasInjected = false;
    public static boolean displayplayers = false;
    public static boolean fullbright = false;
    public static boolean autosprint = false;
    public static boolean drawBlocks = false;
    public static boolean drawBlockTracer = false;
    public static boolean advancedEsp = false;
    public static List<BlockColor> espBlockList = new ArrayList<>();
    public static int RADIUS = 128;
    public static int BATCH_SIZE = 200_000;
    public static int SEARCH_INTERVAL = 10_000;
    public static boolean checkPlayerSafety = false;
    public static boolean forwardTunnel = false;
    public static boolean autoCrystal = false;
    public static int crystalAttackTime = 20;
    public static int crystalPlaceTime = 20;
    public static boolean cancelInteraction = false;
    public static boolean autoAnchor = false;
    static long oreSimSeed = 0;
    public static boolean oreSim = false;
    static int oreSimDistance = 0;
    public static Color oreSimColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    public static boolean autoTotem = false;
    public static int autoTotemDelay = 50;
    public static int autoTotemHumanity = 0;
    public static boolean triggerAutoSell = false;
    public static int autoSellDelay = 300;
    public static String autoSellPrice = "0";
    public static int[] autoSellEndpoints = {0, 8};
    public static boolean autoDcPrimed = false;
    public static float autoDcProximity = 30.0f;
    public static int filterMode = -1;
    public static boolean chatFilter = false;
    public static String blockMsg = "";
    public static boolean aimAssistToggle = false;

    public static void readConfig() {
        if (!safe || out == null || in == null) {
            System.out.println("Check 1 failed: Not safe or streams null");
            return;
        }

        try {
            // Check if socket is still connected
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                safe = false;
                System.out.println("Check 2 failed: Socket not connected");
                return;
            }

            // Set timeout before sending data
            socket.setSoTimeout(100);

            out.println("GET_CONFIG");
            out.flush();

            String response = in.readLine();
            if (response == null) {
                System.out.println("Connection closed by server");
                safe = false;
                return;
            }

            JsonReader reader = new JsonReader(new StringReader(response));
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();


            displayplayers = json.get("displayPlayers").getAsBoolean();
            drawBlocks = json.get("drawBlocks").getAsBoolean();
            RADIUS = json.get("espRadius").getAsInt();
            BATCH_SIZE = json.get("espBatchSize").getAsInt() * 1000;
            SEARCH_INTERVAL = json.get("espSearchTime").getAsInt() * 1000;
            checkPlayerSafety = json.get("checkPlayerAirSafety").getAsBoolean();
            drawBlockTracer = json.get("drawBlockTracer").getAsBoolean();
            advancedEsp = json.get("advEsp").getAsBoolean();
            forwardTunnel = json.get("forwardTunnel").getAsBoolean();
            autoCrystal = json.get("autoCrystal").getAsBoolean();
            crystalAttackTime = json.get("crystalAttackTime").getAsInt();
            crystalPlaceTime = json.get("crystalPlaceTime").getAsInt();
            cancelInteraction = json.get("cancelInteraction").getAsBoolean();
            autoAnchor = json.get("autoAnchor").getAsBoolean();
            oreSimSeed = json.get("oreSimSeed").getAsLong();
            oreSimDistance = json.get("oreSimDistance").getAsInt();
            oreSim = json.get("oreSim").getAsBoolean();
            autoTotem = json.get("autoTotem").getAsBoolean();
            autoTotemDelay = json.get("autoTotemDelay").getAsInt();
            autoTotemHumanity = json.get("autoTotemHumanity").getAsInt();
            triggerAutoSell = json.get("triggerAutoSell").getAsBoolean();
            autoSellDelay = json.get("autoSellDelay").getAsInt();
            autoSellPrice = json.get("autoSellPrice").getAsString();
            autoSellEndpoints[0] = json.get("autoSellEndpointStart").getAsInt();
            autoSellEndpoints[1] = json.get("autoSellEndpointStop").getAsInt();
            autoDcPrimed = json.get("autoDcPrimed").getAsBoolean();
            autoDcProximity = json.get("autoDcCondition").getAsFloat();
            filterMode = json.get("filterMode").getAsInt();
            chatFilter = json.get("chatFilter").getAsBoolean();
            blockMsg = json.get("blockMsg").getAsString();
            aimAssistToggle = json.get("aimAssistToggle").getAsBoolean();

            JsonArray oreSimColArr = json.get("oreSimColor").getAsJsonArray();
            oreSimColor = new Color(
                    oreSimColArr.get(0).getAsFloat(),
                    oreSimColArr.get(1).getAsFloat(),
                    oreSimColArr.get(2).getAsFloat(),
                    oreSimColArr.get(3).getAsFloat()
            );

            if (oreSim) {
                if (MinecraftClient.getInstance().world != null && (oreSimSeed != lastOreSimSeed || oreSimDistance != lastOreSimDistance)) {
                    OreSimulator.setWorldSeed(oreSimSeed);
                    OreSimulator.setHorizontalRadius(oreSimDistance);
                    OreSimulator.recalculateChunks();
                    lastOreSimSeed = oreSimSeed;
                    lastOreSimDistance = oreSimDistance;
                }
            }

            JsonArray espBlockArray = json.getAsJsonArray("espBlockList");
            if (espBlockArray == null || espBlockArray.isEmpty()) return;

            List<BlockColor> newBlockList = new ArrayList<>();
            Set<Identifier> validIds = new HashSet<>();

            for (JsonElement element : espBlockArray) {
                try {
                    JsonObject blockObj = element.getAsJsonObject();

                    // 1. Get and validate block ID
                    String blockIdStr = blockObj.get("name").getAsString();
                    Identifier id = Identifier.tryParse(blockIdStr);
                    if (id == null) {
                        System.err.println("Invalid block ID format: " + blockIdStr);
                        continue;
                    }

                    // 2. Get block from registry
                    Block block = Registries.BLOCK.get(id);
                    if (block == Blocks.AIR) {
                        System.err.println("Block not found in registry: " + blockIdStr);
                        continue;
                    }

                    // 3. Get and validate color array
                    JsonArray colorArray = blockObj.get("color").getAsJsonArray();
                    if (colorArray.size() != 4) {
                        System.err.println("Invalid color format for block: " + blockIdStr);
                        continue;
                    }

                    Color blockColor = new Color(
                            colorArray.get(0).getAsFloat(),
                            colorArray.get(1).getAsFloat(),
                            colorArray.get(2).getAsFloat(),
                            colorArray.get(3).getAsFloat()
                    );

                    // 4. Get enabled state (default to true if not specified)
                    boolean enabled = !blockObj.has("enabled") || blockObj.get("enabled").getAsBoolean();

                    // 5. Add to list regardless of enabled state so we track it
                    BlockColor blockColorEntry = new BlockColor(block, blockColor, enabled);
                    newBlockList.add(blockColorEntry);

                    if (enabled) {
                        validIds.add(id);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing block entry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            espBlockList.clear();
            espBlockList.addAll(newBlockList);

        } catch (SocketTimeoutException e) {
            System.err.println("Timeout waiting for server response");
            safe = false;
        } catch (SocketException e) {
            System.err.println("Socket error: " + e.getMessage());
            safe = false;
            cleanupConnection(); // Ensure the socket is cleaned up if an error occurs
        } catch (Exception e) {
            System.err.println("Error in readConfig: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            safe = false;
            cleanupConnection();
        } finally {
            try {
                if (socket != null) {
                    socket.setSoTimeout(0);  // Reset timeout after operation
                }
            } catch (Exception e) {
                System.err.println("Failed to reset timeout: " + e.getMessage());
            }
        }
    }

    private static void cleanupConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up connection: " + e.getMessage());
        }
        out = null;
        in = null;
        socket = null;
    }


    public static void writeRenderFlag(boolean shouldRender, boolean worldReady) {
        try {
            if (out == null) {
                System.err.println("Output stream is null, cannot send render flag.");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("shouldRender", shouldRender);
            json.addProperty("worldReady", worldReady);
            out.println("RENDER_FLAG " + json);
            out.flush();
        } catch (Exception e) {
            System.err.println("Failed to send render flag: " + e.getMessage());
            safe = false;
        }
    }


    public static void writePlayerList(List<String> players) {
        try {
            // Safety check before sending data through the output stream
            if (out != null) {
                JsonObject json = new JsonObject();
                JsonArray playerArray = new JsonArray();
                for (String player : players) playerArray.add(player);

                json.add("PLAYERS", playerArray);

                out.println("PLAYERS " + json);
                out.flush();
            } else {
                System.err.println("Output stream is null, cannot send player list.");
                safe = false;
            }
        } catch (Exception e) {
            System.err.println("Failed to send player list: " + e.getMessage());
            safe = false;
        }
    }

    public static void writePlayerSaftey(String safety) {
        try {
            if (out == null) {
                System.err.println("Output stream is null, cannot send render flag.");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("playerAirSafety", safety);
            out.println("playerAirSafety " + json);
            out.flush();
        } catch (Exception e) {
            System.err.println("Failed to send player safety: " + e.getMessage());
            safe = false;
        }
    }

    public static void writeBlockStatus(String blkSts) {
        try {
            if (out == null) {
                System.err.println("Output stream is null, cannot send render flag.");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("tunnelBlockStatus", blkSts);
            out.println("tunnelBlockStatus " + json);
            out.flush();
        } catch (Exception e) {
            System.err.println("Failed to send tunnel block status: " + e.getMessage());
            safe = false;
        }
    }

    public static void sendAutoDcFlag(boolean flag) {
        try {
            if (out == null) return;

            JsonObject json = new JsonObject();
            json.addProperty("autoDcPrimedDisable", flag);
            out.println("autoDcPrimedDisable " + json);
            out.flush();
        } catch (Exception e) { safe = false; }
    }
}
