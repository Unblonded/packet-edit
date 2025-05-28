package unblonded.packets;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unblonded.packets.imgui.ImGuiManager;
import unblonded.packets.util.ConfigManager;
import unblonded.packets.util.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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
			String hwid = getHWID();
			console.error("Authentication failed. Exiting...");
			console.info("Your HWID: " + hwid);
			util.showHWIDPopup(hwid);
			util.crash();
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			ImGuiManager.getInstance().init();
			ConfigManager.loadConfig();
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			ConfigManager.saveConfig();
		});

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
		String hwid = getHWID();

		try {
			String postData = "{ \"username\": \"" + username + "\", \"hwid\": \"" + hwid + "\" }";
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

				// Use the provided handler to transform the response into type T
				return responseHandler.apply(responseString);
			}

		} catch (Exception e) {
			System.out.println("Error contacting server: " + e.getMessage());
			e.printStackTrace();
			return null; // or responseHandler.apply("error") if you want to handle errors through handler
		}
	}

	static String getHWID() {
		try {
			StringBuilder sb = new StringBuilder();

			// Get processor serial
			String processorID = System.getenv("PROCESSOR_IDENTIFIER");
			if (processorID != null) {
				sb.append(processorID);
			}

			// Get motherboard serial
			String motherboardSerial = getMotherboardSerial();
			if (motherboardSerial != null && !motherboardSerial.isEmpty()) {
				sb.append(motherboardSerial);
			}

			// Get disk drive serial
			String diskDriveSerial = getDiskDriveSerial();
			if (diskDriveSerial != null && !diskDriveSerial.isEmpty()) {
				sb.append(diskDriveSerial);
			}

			// Get MAC address
			try {
				NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
				if (network != null) {
					byte[] mac = network.getHardwareAddress();
					if (mac != null) {
						for (byte b : mac) {
							sb.append(String.format("%02X", b));
						}
					}
				}
			} catch (Exception ignored) {}

			// If couldn't get hardware info, fall back to java runtime
			if (sb.length() < 6) {
				sb.append(System.getProperty("java.runtime.name"))
						.append(System.getProperty("java.vm.name"))
						.append(System.getProperty("os.arch"));
			}

			// Generate MD5 hash of the collected system info
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(sb.toString().getBytes());

			// Convert MD5 hash to hexadecimal string
			StringBuilder hexString = new StringBuilder();
			for (byte b : digest) {
				hexString.append(String.format("%02x", b));
			}

			return hexString.toString();
		} catch (Exception e) {
			// Return a fallback ID if everything fails
			return UUID.randomUUID().toString();
		}
	}

	// Helper method to get motherboard serial
	private static String getMotherboardSerial() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "wmic", "baseboard", "get", "serialnumber" });
			process.getOutputStream().close();
			Scanner sc = new Scanner(process.getInputStream());
			String output = "";
			while (sc.hasNext()) {
				String line = sc.nextLine().trim();
				if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
					output = line;
					break;
				}
			}
			sc.close();
			return output;
		} catch (Exception e) {
			return "";
		}
	}

	private static String getDiskDriveSerial() {
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "wmic", "diskdrive", "get", "serialnumber" });
			process.getOutputStream().close();
			Scanner sc = new Scanner(process.getInputStream());
			String output = "";
			while (sc.hasNext()) {
				String line = sc.nextLine().trim();
				if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
					output = line;
					break;
				}
			}
			sc.close();
			return output;
		} catch (Exception e) {
			return "";
		}
	}
}
