package unblonded.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                } catch (IOException e) {
                    System.out.println("Waiting for injection...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        connectionThread.start();
    }

    public static boolean hasInjected = false;
    public static boolean displayplayers = false;
    public static boolean fullbright = false;
    public static boolean autosprint = false;
    public static boolean drawBlocks = false;
    public static boolean drawBlockTracer = false;
    public static List<Block> espBlockList = new ArrayList<>();
    public static int RADIUS = 128;
    public static int BATCH_SIZE = 200_000;
    public static int SEARCH_INTERVAL = 10_000;
    public static boolean checkPlayerSafety = false;

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

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            displayplayers = json.get("displayPlayers").getAsBoolean();
            drawBlocks = json.get("drawBlocks").getAsBoolean();
            RADIUS = json.get("espRadius").getAsInt();
            BATCH_SIZE = json.get("espBatchSize").getAsInt() * 1000;
            SEARCH_INTERVAL = json.get("espSearchTime").getAsInt() * 1000;
            checkPlayerSafety = json.get("checkPlayerAirSafety").getAsBoolean();
            drawBlockTracer = json.get("drawBlockTracer").getAsBoolean();

            JsonArray espBlockArray = json.getAsJsonArray("espBlockList");
            for (int i = 0; i < espBlockArray.size(); i++) {
                String blockId = espBlockArray.get(i).getAsString();
                try {
                    ResourceLocation id = ResourceLocation.tryParse(blockId);
                    if (id != null) {
                        Optional<Block> maybeBlock = BuiltInRegistries.BLOCK.getOptional(id);
                        if (maybeBlock.isPresent()) {
                            Block block = maybeBlock.get();
                            if (block != Blocks.AIR && !espBlockList.contains(block)) {
                                espBlockList.add(block);
                            }
                        } else {
                            System.err.println("Block not found in registry: " + blockId);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing block ID: " + blockId + " -> " + e.getMessage());
                }
            }

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


    public static void writeRenderFlag(boolean shouldRender) {
        try {
            if (out == null) {
                System.err.println("Output stream is null, cannot send render flag.");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("shouldRender", shouldRender);
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

    public static void writePlayerSaftey(boolean safety) {
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

}
