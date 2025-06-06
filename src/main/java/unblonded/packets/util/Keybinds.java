package unblonded.packets.util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding openGui = new KeyBinding("key.packetedit.open_gui", GLFW.GLFW_KEY_G, "category.packetedit");


    public static void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(openGui);
    }

    public static int getKeycode(KeyBinding key) {
        return key.getDefaultKey().getCode();
    }

}
