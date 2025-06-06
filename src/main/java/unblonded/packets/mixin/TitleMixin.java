package unblonded.packets.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mixin(Window.class)
public class TitleMixin {
    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "setTitle", at = @At(value = "HEAD"), cancellable = true)
    public void redirectSetTitle(String title, CallbackInfo ci) {
        ci.cancel();

        String customTitle;

        if (client.world != null) {
            String suffix = " - Packet Edit by Unblonded ❤";
            String worldName = client.world.getRegistryKey().getValue().getPath();
            String formattedTitle = Arrays.stream(worldName.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
            customTitle = formattedTitle + suffix;
        } else customTitle = "Packet Edit by Unblonded ❤";

        GLFW.glfwSetWindowTitle(client.getWindow().getHandle(), customTitle);
    }
}