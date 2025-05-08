package unblonded.packets.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cfg;
import unblonded.packets.cheats.OreSimulator;
import unblonded.packets.util.util;

@Mixin(MinecraftClient.class)
public abstract class OnTickMixin {
    @Unique private int tickCount = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!cfg.hasInjected && tickCount++ > 40) util.inject(client);
        util.handleKeyInputs(client);
        util.updateUI(client);
        util.updateOreSim(client);
        util.updateStates();
    }
}
