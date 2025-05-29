package unblonded.packets;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unblonded.packets.imgui.ImGuiManager;
import unblonded.packets.util.ConfigManager;
import unblonded.packets.util.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;

public class Packetedit {
	public static final String MOD_ID = "packet-edit";
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final Logger console = LoggerFactory.getLogger(MOD_ID);

	public static void onInitializeClient() {
		boolean serverStatus = Boolean.TRUE.equals(contactServer(util.decrypt(util.decrypt(util.encrypt("aHR0cHM6Ly9hcGkucGFja2V0ZWRpdC50b3AvbG9naW4="))), response -> response.contains("success")));

		if (!serverStatus) {
			console.error("Authentication failed. Exiting...");
			util.crash();
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {ConfigManager.loadConfig();});

		console.info("Authentication successful!");
	}

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
	}

	public static boolean isMouseKeyDown(int key) {
		return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
	}

	public static <T> T contactServer(String urlString, Function<String, T> responseHandler) {
		String username = MinecraftClient.getInstance().getSession().getUsername();

		try {
			String postData = "{ \"username\": \"" + username + "\" }";
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = postData.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();

			InputStream inputStream = (responseCode >= 200 && responseCode <= 299)
					? connection.getInputStream()
					: connection.getErrorStream();

			try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					response.append(line.trim());
				}
				String responseString = response.toString();

				return responseHandler.apply(responseString);
			}

		} catch (Exception e) {
			System.out.println("Error contacting server: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static File workDir() {
		return new File(mc.runDirectory, "packet-edit");
	}
}
