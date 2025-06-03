package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import unblonded.packets.util.CmdManager.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GodSwordCommand extends Command {
    public GodSwordCommand() {
        super("godsword", "Gives god sword only in creative mode");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.executes(context -> {
            ClientPlayerEntity player = context.getSource().getPlayer();

            if (!player.getAbilities().creativeMode) {
                context.getSource().sendError(Text.literal("§cThis command can only be used in Creative mode!"));
                return 0;
            }

            ItemStack godSword = new ItemStack(Items.NETHERITE_SWORD);

            Registry<Enchantment> enchantmentRegistry = player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            ItemEnchantmentsComponent.Builder enchantmentsBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.SHARPNESS.getValue()).get(), 255);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.UNBREAKING.getValue()).get(), 255);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.LOOTING.getValue()).get(), 255);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.FIRE_ASPECT.getValue()).get(), 255);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.KNOCKBACK.getValue()).get(), 255);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.SWEEPING_EDGE.getValue()).get(), 255);
            enchantmentsBuilder.add(enchantmentRegistry.getEntry(Enchantments.MENDING.getValue()).get(), 1);
            godSword.set(DataComponentTypes.ENCHANTMENTS, enchantmentsBuilder.build());

            AttributeModifiersComponent.Builder attributeBuilder = AttributeModifiersComponent.builder();

            attributeBuilder.add(
                    EntityAttributes.ATTACK_DAMAGE,
                    new EntityAttributeModifier(
                            Identifier.of("godsword", "attack_damage"),
                            Double.MAX_VALUE,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    AttributeModifierSlot.MAINHAND
            );
            attributeBuilder.add(
                    EntityAttributes.ATTACK_SPEED,
                    new EntityAttributeModifier(
                            Identifier.of("godsword", "attack_speed"),
                            100.0,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    AttributeModifierSlot.MAINHAND
            );

            godSword.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributeBuilder.build());
            godSword.set(DataComponentTypes.CUSTOM_NAME, Text.literal("⚡ GOD SWORD ⚡").formatted(Formatting.GOLD, Formatting.BOLD));

            List<Text> loreList = List.of(
                    Text.literal("A weapon of immense power").formatted(Formatting.GRAY),
                    Text.literal("forged by the gods themselves").formatted(Formatting.GRAY),
                    Text.empty(),
                    Text.literal("DAMAGE: ∞").formatted(Formatting.RED, Formatting.BOLD)
            );
            godSword.set(DataComponentTypes.LORE, new LoreComponent(loreList));

            if (!player.getInventory().insertStack(godSword)) {
                player.dropItem(godSword, false);
                context.getSource().sendFeedback(Text.literal("§aGod Sword given! §7(Dropped due to full inventory)"));
            } else context.getSource().sendFeedback(Text.literal("§aGod Sword given!"));

            return 1;
        });
    }
}