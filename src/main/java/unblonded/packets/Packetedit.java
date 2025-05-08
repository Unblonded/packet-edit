package unblonded.packets;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static unblonded.packets.InjectorBridge.extractFiles;

public class Packetedit implements ClientModInitializer {
	public static final String MOD_ID = "packet-edit";
	public static final Logger console = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		console.info("Packet Edit is initializing!");
		extractFiles("menu.dll");
		extractFiles("mcInject.exe");
	}

	public static boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
	}
}