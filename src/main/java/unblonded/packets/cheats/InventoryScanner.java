package unblonded.packets.cheats;

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

import java.util.ArrayList;
import java.util.List;

public class InventoryScanner {

    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static KeyBinding toggleSearchBinding;
    public static String searchString = "Diamond";
    public static boolean searchActive = true;
    public static final int GLOW_COLOR = 0xFF33FFFF;

    public static void onInitializeClient() {
        // Register keybinding for toggling search
        toggleSearchBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.inventorysearcher.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I, // Default to 'I' key
                "category.inventorysearcher.general"
        ));

        // Register tick event for handling keybindings
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Toggle search mode when key is pressed
            while (toggleSearchBinding.wasPressed()) {
                searchActive = !searchActive;
                if (searchActive) {
                    // Show search input when toggled on
                }
            }
        });
    }

    public void setSearchString(String query) {
        searchString = query.toLowerCase();
    }

    public static boolean matchesSearch(ItemStack stack) {
        if (searchString.isEmpty()) return false;

        String query = searchString.toLowerCase();
        List<String> textToSearch = new ArrayList<>();

        // Add item name
        textToSearch.add(stack.getName().getString().toLowerCase());

        // Use the standard tooltip API to get lore, enchantments, etc.
        List<Text> tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, TooltipType.ADVANCED);
        for (Text line : tooltip) {
            textToSearch.add(line.getString().toLowerCase());
        }

        for (String text : textToSearch) {
            if (text.contains(query)) {
                return true;
            }
        }

        return false;
    }

}