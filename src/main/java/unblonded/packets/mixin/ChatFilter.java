package unblonded.packets.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cfg;

@Mixin(ChatHud.class)
public class ChatFilter {
    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        String plain = message.getString();
        if (cfg.chatFilter) {
            if (cfg.filterMode == 0) ci.cancel();
            if (cfg.filterMode == 1) {
                if (plain.contains(cfg.blockMsg)) {
                    ci.cancel();
                }
            }
            if (cfg.filterMode == 2) {
                if (!plain.contains(cfg.blockMsg)) {
                    ci.cancel();
                }
            }
        }
    }
}

