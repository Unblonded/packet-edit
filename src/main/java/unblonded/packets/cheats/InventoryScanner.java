package unblonded.packets.cheats;

import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import unblonded.packets.mixin.HandledScreenAccessor;
import unblonded.packets.util.Color;

import java.util.ArrayList;
import java.util.List;

public class InventoryScanner {

    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static String searchString = "";
    private static Color glowColor = new Color();
    private static boolean enabled = false;

    public static boolean matchesSearch(ItemStack stack) {
        if (searchString.isEmpty() || !enabled) return false;

        String query = searchString.toLowerCase();
        List<String> textToSearch = new ArrayList<>();

        textToSearch.add(stack.getName().getString().toLowerCase());

        List<Text> tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, TooltipType.ADVANCED);
        for (Text line : tooltip) textToSearch.add(line.getString().toLowerCase());

        for (String text : textToSearch)
            if (text.contains(query))
                return true;

        return false;
    }

    public static void drawHighlights(DrawContext context, HandledScreen<?> screen) {
        if (searchString.isEmpty() || !enabled) return;

        HandledScreenAccessor screenAccessor = (HandledScreenAccessor) screen;
        int containerX = screenAccessor.getX();
        int containerY = screenAccessor.getY();

        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && matchesSearch(stack)) {
                int absoluteX = containerX + slot.x;
                int absoluteY = containerY + slot.y;

                context.fill(absoluteX, absoluteY, absoluteX + 16, absoluteY + 16, glowColor.asHex());
            }
        }
    }

    public static void setState(ImBoolean state, ImString search, float[] color) {
        enabled = state.get();
        searchString = search.get();
        glowColor = new Color(color);
    }
}