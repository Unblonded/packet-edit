package unblonded.packets.util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Keybinds implements ClientModInitializer {
    public static final KeyMapping openGui = new KeyMapping("key.packetedit.open_gui", GLFW.GLFW_KEY_G, "category.packetedit");


    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(openGui);
    }

    public static int getKeycode(KeyMapping key) {
        return key.getDefaultKey().getValue();
    }

}
