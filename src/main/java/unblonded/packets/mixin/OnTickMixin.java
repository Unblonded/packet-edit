package unblonded.packets.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cfg;
import unblonded.packets.util.util;

@Mixin(Minecraft.class)
public abstract class OnTickMixin {
    @Unique private int tickCount = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!cfg.hasInjected && tickCount++ > 40) util.inject(client);
        util.handleKeyInputs(client);
        util.updateUI(client);
        util.updateStates();
    }
}
