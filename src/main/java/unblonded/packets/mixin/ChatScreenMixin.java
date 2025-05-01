package unblonded.packets.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.Packetedit;

import static unblonded.packets.Packetedit.addCommandToHistory;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void onSend(String chatText, boolean addToHistory, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (chatText.contains(".help")) {
            ci.cancel();
            addCommandToHistory(chatText);
            client.gui.getChat().addMessage(Component.literal("Commands: .vclip <distance>, ... more coming soon!"));
        }

        if (chatText.contains(".vclip")) {
            ci.cancel();
            addCommandToHistory(chatText);
            String[] args = chatText.split(" ");
            if (args.length < 2) {
                client.gui.getChat().addMessage(Component.literal("Usage: .vclip <distance>"));
                return;
            }

            try {
                double dist = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                client.gui.getChat().addMessage(Component.literal("Invalid number for vclip."));
            }
        }
    }
}

