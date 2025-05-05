package unblonded.packets;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unblonded.packets.util.GuiBackground;
import unblonded.packets.util.Keybinds;

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
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
	}

	public static void addCommandToHistory(String chatText) {
		Minecraft client = Minecraft.getInstance();
        client.gui.getChat().addRecentChat(chatText);
    }

}