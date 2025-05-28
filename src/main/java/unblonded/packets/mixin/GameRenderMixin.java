package unblonded.packets.mixin;

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
import unblonded.packets.imgui.Menu;

@Mixin(GameRenderer.class)
public class GameRenderMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void hookTail(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        ImGuiManager.getInstance().newFrame();

        if (!cfg.fontSizeOverride.get()) {
            ImVec2 windowSize = ImGui.getIO().getDisplaySize();
            float scale = Math.max(1.2f, (windowSize.x / 1920.0f) * 0.85f);
            ImGui.getIO().setFontGlobalScale(scale);
        } else ImGui.getIO().setFontGlobalScale(cfg.fontSize[0]);

        Menu.render();
        ImGuiManager.getInstance().render();
    }
}
