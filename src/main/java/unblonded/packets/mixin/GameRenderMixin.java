package unblonded.packets.mixin;

import imgui.ImGuiIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import unblonded.packets.cfg;
import unblonded.packets.imgui.ImGuiManager;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.imgui.ImGuiThemes;
import unblonded.packets.imgui.Menu;
import unblonded.packets.util.GuiBackground;

@Mixin(GameRenderer.class)
public class GameRenderMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void hookTail(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!ImGuiManager.getInstance().isInit()) ImGuiManager.getInstance().init();

        if (ImGuiManager.getInstance().isInit()) {
            MinecraftClient client = MinecraftClient.getInstance();
            ImGuiManager.getInstance().newFrame();

            ImGuiIO io = ImGui.getIO();
            io.setMouseDown(0, !client.mouse.isCursorLocked() && io.getMouseDown(0));

            if (!cfg.fontSizeOverride.get()) {
                ImVec2 windowSize = ImGui.getIO().getDisplaySize();
                float scale = Math.max(1.1f, (windowSize.x / 1920.0f) * 0.85f);
                ImGui.getIO().setFontGlobalScale(scale);
            } else {
                ImGui.getIO().setFontGlobalScale(cfg.fontSize[0]);
            }

            cfg.showMenu = client.currentScreen instanceof GuiBackground;
            cfg.showAll = client.world != null;
            cfg.storageScanShow = client.currentScreen instanceof HandledScreen;

            Menu.render();

            if (cfg.nightFx.get() && client.currentScreen == null) ImGuiThemes.cosmicCrosshair();

            ImGuiManager.getInstance().render();
        }
    }
}
