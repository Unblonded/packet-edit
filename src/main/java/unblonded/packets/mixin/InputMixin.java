package unblonded.packets.mixin;

import imgui.ImGui;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class InputMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKeyInject(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (ImGui.getIO().getWantTextInput()) ci.cancel();
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onCharInject(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (ImGui.getIO().getWantTextInput()) ci.cancel();
    }
}
