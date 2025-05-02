package unblonded.packets.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.InjectorBridge;
import unblonded.packets.Packetedit;
import unblonded.packets.cfg;
import unblonded.packets.cheats.AirUnderCheck;
import unblonded.packets.cheats.AutoSprint;
import net.minecraft.client.gui.screens.ChatScreen;

import static unblonded.packets.cfg.*;
import static unblonded.packets.cheats.PlayerTracker.getNearbyPlayers;

@Mixin(Minecraft.class)
public abstract class OnTickMixin {
    @Unique private int tickCount = 0;
    @Shadow protected abstract void openChatScreen(String text);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!hasInjected && tickCount++ > 100) { // ~5 seconds after launch
            InjectorBridge.runExecutable("mcInject.exe");
            cfg.init();
            hasInjected = true;
        }
        if (Packetedit.isKeyDown(GLFW.GLFW_KEY_PERIOD) && Minecraft.getInstance().screen == null) openChatScreen(".");
        AutoSprint.run();
        if (cfg.safe) {
            cfg.readConfig();
            boolean worldLoaded = Minecraft.getInstance().level != null;
            writeRenderFlag(Minecraft.getInstance().screen instanceof ChatScreen, worldLoaded);
        }

        if (cfg.displayplayers) writePlayerList(getNearbyPlayers());

        if (cfg.checkPlayerSafety) {
            AirUnderCheck.checkSafety();
            writePlayerSaftey(AirUnderCheck.playerAirSafety);
        }
    }
}
