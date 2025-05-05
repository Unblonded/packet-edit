package unblonded.packets.cheats;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.List;

public class InteractionCanceler implements ClientModInitializer {
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
            EntityType.COMMAND_BLOCK_MINECART,
            EntityType.PLAYER
    );

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.level == null) return;

            for (Entity entity : client.level.entitiesForRendering()) {
                if (GHOST_ENTITIES.contains(entity.getType())) {
                    entity.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
                }
            }
        });

        // Make ghost entities non-interactable
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (enabled && GHOST_ENTITIES.contains(entity.getType())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }

    public static void setState(boolean state) {
        enabled = state;
    }
}