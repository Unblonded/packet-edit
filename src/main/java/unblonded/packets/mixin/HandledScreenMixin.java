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
        if (!(InventoryScanner.CLIENT.currentScreen instanceof HandledScreen<?> handledScreen)) return;
        if (InventoryScanner.searchString.isEmpty()) return;

        HandledScreenAccessor screenAccessor = (HandledScreenAccessor) this;
        int containerX = screenAccessor.getX();
        int containerY = screenAccessor.getY();

        ScreenHandler handler = handledScreen.getScreenHandler();

        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && InventoryScanner.matchesSearch(stack)) {
                int absoluteX = containerX + slot.x;
                int absoluteY = containerY + slot.y;

                context.fill(absoluteX, absoluteY, absoluteX + 16, absoluteY + 16, InventoryScanner.GLOW_COLOR);
            }
        }
    }
}
