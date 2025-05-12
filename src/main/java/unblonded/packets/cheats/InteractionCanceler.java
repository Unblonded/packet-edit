package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;

import java.util.Arrays;
import java.util.List;

public class InteractionCanceler {
    private static boolean enabled = true;

    private static final List<EntityType<?>> GHOST_ENTITIES = Arrays.asList(
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER,
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.ARMOR_STAND,
            EntityType.MINECART,
            EntityType.CHEST_MINECART,
            EntityType.FURNACE_MINECART,
            EntityType.TNT_MINECART,
            EntityType.HOPPER_MINECART,
            EntityType.COMMAND_BLOCK_MINECART
    );

    public static void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.world == null) return;

            for (Entity entity : client.world.getEntities()) {
                if (GHOST_ENTITIES.contains(entity.getType())) {
                    entity.setBoundingBox(new Box(0, 0, 0, 0, 0, 0));
                }
            }
        });

        // Make ghost entities non-interactable
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (enabled && GHOST_ENTITIES.contains(entity.getType())) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    public static void setState(boolean state) {
        enabled = state;
    }
}