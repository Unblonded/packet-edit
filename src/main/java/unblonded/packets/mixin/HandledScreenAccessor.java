package unblonded.packets.mixin;// Replace with your actual package

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();

    // You might need accessors for other fields like titleX or titleY as well
    // @Accessor("titleX")
    // int getTitleX();

    // @Accessor("titleY")
    // int getTitleY();
}