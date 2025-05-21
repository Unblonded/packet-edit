package unblonded.packets.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import unblonded.packets.cheats.SelfCrystal;

@Mixin(ClientPlayerInteractionManager.class)
public class InteractionManager {
    @Inject(at = @At("HEAD"), method = "interactBlock")
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult,
                                 CallbackInfoReturnable<?> cir) {
        ItemStack stack = player.getStackInHand(hand);
        SelfCrystal.onPlayerInteract(stack, hand, hitResult);
    }
}