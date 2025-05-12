package unblonded.packets.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatMixin {
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSend(String chatText, boolean addToHistory, CallbackInfo ci) {

    }
}
