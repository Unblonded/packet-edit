package unblonded.packets.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuiBackground extends Screen {
    public GuiBackground(Component title) { super(title);}
    @Override public boolean isPauseScreen() { return false; }
    @Override public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override public boolean keyPressed(int k1, int k2, int k3) {
        if (k1 == Keybinds.getKeycode(Keybinds.openGui) || k1 == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return false;
    }
}
