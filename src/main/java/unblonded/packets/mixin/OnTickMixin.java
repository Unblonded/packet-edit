package unblonded.packets.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cheats.CrystalSpam;
import unblonded.packets.util.ConfigManager;
import unblonded.packets.util.util;

@Mixin(MinecraftClient.class)
public abstract class OnTickMixin {
    @Unique private int tickCount = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (tickCount > Integer.MAX_VALUE - 100) tickCount = 0;
        else tickCount++;
        util.handleKeyInputs(client);
        util.updateOreSim(client);
        util.updateStates();
        util.setTitle(client);
        CrystalSpam.start();

        if (tickCount % 200 == 0) ConfigManager.saveConfig();
    }
}
