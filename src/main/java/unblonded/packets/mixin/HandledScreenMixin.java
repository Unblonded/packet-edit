package unblonded.packets.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unblonded.packets.cheats.InventoryScanner;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void draw(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (InventoryScanner.CLIENT.currentScreen instanceof HandledScreen<?> screen) {
            InventoryScanner.drawHighlights(context, screen);
        }
    }
}
