package unblonded.packets.util;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.Component;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class GuiBackground extends Screen {
    public GuiBackground(Text title) { super(title);}
    @Override public boolean shouldPause() { return false; }

    @Override public boolean keyPressed(int k1, int k2, int k3) {
        if (k1 == Keybinds.getKeycode(Keybinds.openGui) || k1 == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(null);
            return true;
        }
        return false;
    }
}
