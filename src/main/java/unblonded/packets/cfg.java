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
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

public class cfg {
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    public static boolean safe = false;
    private static Thread connectionThread;

    public static void init() {
        connectionThread = new Thread(() -> {
            while (socket == null && !Thread.currentThread().isInterrupted()) {
                try {
                    socket = new Socket("127.0.0.1", 1337);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    safe = true;
                    System.out.println("Connected to C++ server.");
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.getWindow().setTitle("Packet Edit v3 - Ready");
                } catch (IOException e) {
                    //System.out.println("Waiting for injection...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        connectionThread.start();
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
            reader.setStrictness(Strictness.LENIENT); // requires Gson 2.10+
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
            Set<Identifier> jsonBlockIds = new HashSet<>();
            for (JsonElement element : espBlockArray) {
                JsonObject blockObj = element.getAsJsonObject();
                String blockId = blockObj.get("name").getAsString();
                Identifier  id = Identifier.tryParse(blockId);
                if (id != null) {
                    jsonBlockIds.add(id);
                }
            }

            for (JsonElement element : espBlockArray) {
                JsonObject blockObj = element.getAsJsonObject();
                String blockIdStr = blockObj.get("name").getAsString();
                JsonArray colorArray = blockObj.get("color").getAsJsonArray();

                try {
                    Identifier id = Identifier.tryParse(blockIdStr);

                    Block block = Registries.BLOCK.get(id);
                    if (block == Blocks.AIR) return;

                    Color blockColor = new Color(
                            colorArray.get(0).getAsFloat(),
                            colorArray.get(1).getAsFloat(),
                            colorArray.get(2).getAsFloat(),
                            colorArray.get(3).getAsFloat()
                    );
                    espBlockList.removeIf(bc -> bc.getBlock().equals(block));
                    espBlockList.add(new BlockColor(block, blockColor));

                } catch (Exception e) {
                    System.err.println("Error processing block: " + blockIdStr + " -> " + e.getMessage());
                }
            }
            espBlockList.removeIf(bc -> {
                Identifier id = Registries.BLOCK.getId(bc.getBlock());
                return !jsonBlockIds.contains(id);
            });

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
}
