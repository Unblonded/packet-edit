package unblonded.packets;

import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unblonded.packets.cheats.OreSimulator;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static unblonded.packets.InjectorBridge.extractFiles;

public class Packetedit implements ClientModInitializer {
	public static final String MOD_ID = "packet-edit";
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final Logger console = LoggerFactory.getLogger(MOD_ID);

	private OreSimulator simulator;
	private long currentSeed = 0;
	private boolean enabled = false;
	private int renderRadius = 5;

	@Override
	public void onInitializeClient() {
		console.info("Packet Edit is initializing!");
		extractFiles("menu.dll");
		extractFiles("mcInject.exe");
	}

	private void sendMessage(String message, Formatting color) {
		if (mc.player != null) {
			mc.player.sendMessage(Text.literal("[Ancient Debris Finder] " + message).setStyle(Style.EMPTY.withColor(color)), false);
		}
	}

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
	}
}