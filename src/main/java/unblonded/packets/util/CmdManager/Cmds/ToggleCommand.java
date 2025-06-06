package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import imgui.type.ImBoolean;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import unblonded.packets.cfg;
import unblonded.packets.util.Chat;
import unblonded.packets.util.CmdManager.Command;
import unblonded.packets.util.Toggleable;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ToggleCommand extends Command {

    private static final Map<String, ToggleableFeature> FEATURE_MAP = new HashMap<>();
    private static final String[] FEATURE_ALIASES;

    static {
        initializeFeatures();
        FEATURE_ALIASES = FEATURE_MAP.keySet().toArray(new String[0]);
    }

    // Data class to hold feature information
    private static class ToggleableFeature {
        final Field field;
        final String displayName;
        final String category;
        final String[] aliases;

        ToggleableFeature(Field field, Toggleable annotation) {
            this.field = field;
            this.displayName = annotation.displayName();
            this.category = annotation.category();
            this.aliases = annotation.aliases();
        }
    }

    // Initialize features by scanning the cfg class for @Toggleable annotations
    private static void initializeFeatures() {
        Field[] fields = cfg.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Toggleable.class)) {
                Toggleable annotation = field.getAnnotation(Toggleable.class);
                ToggleableFeature feature = new ToggleableFeature(field, annotation);

                // Register all aliases for this feature
                for (String alias : annotation.aliases()) {
                    FEATURE_MAP.put(alias.toLowerCase(), feature);
                }
            }
        }
    }

    // Suggestion provider for autocomplete
    private static final SuggestionProvider<FabricClientCommandSource> FEATURE_SUGGESTIONS =
            (context, builder) -> CommandSource.suggestMatching(FEATURE_ALIASES, builder);

    public ToggleCommand() {
        super("t", "Toggles a feature on or off");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.then(argument("feature", StringArgumentType.string())
                        .suggests(FEATURE_SUGGESTIONS)
                        .executes(this::toggleFeature))
                .executes(context -> {
                    Chat.sendMessage(Chat.prefix + " Usage: .t <feature>");
                    Chat.sendMessage(Chat.prefix + " Available features: " + getAvailableFeatures());
                    return 1;
                });
    }

    private int toggleFeature(CommandContext<FabricClientCommandSource> context) {
        String input = StringArgumentType.getString(context, "feature").toLowerCase();

        ToggleableFeature feature = FEATURE_MAP.get(input);
        if (feature == null) {
            Chat.sendMessage(Chat.prefix + " §cUnknown feature: " + input);
            Chat.sendMessage(Chat.prefix + " Available features: " + getAvailableFeatures());
            return 0;
        }

        try {
            // Get the current value and toggle it
            Object fieldValue = feature.field.get(null);

            if (fieldValue instanceof ImBoolean) {
                ImBoolean imBool = (ImBoolean) fieldValue;
                imBool.set(!imBool.get());

                String status = imBool.get() ? "§aenabled" : "§cdisabled";
                Chat.sendMessage(Chat.prefix + " §e" + feature.displayName + " " + status);
                return 1;
            } else {
                Chat.sendMessage(Chat.prefix + " §cError: " + feature.displayName + " is not a toggleable boolean field");
                return 0;
            }

        } catch (IllegalAccessException e) {
            Chat.sendMessage(Chat.prefix + " §cError accessing field: " + feature.displayName);
            return 0;
        }
    }

    // Helper method to get a formatted list of available features
    private String getAvailableFeatures() {
        Set<String> uniqueFeatures = FEATURE_MAP.values().stream()
                .map(f -> f.displayName)
                .collect(Collectors.toSet());

        return String.join(", ", uniqueFeatures);
    }

    // Optional: Method to list features by category
    public static void listFeaturesByCategory() {
        Map<String, List<ToggleableFeature>> byCategory = FEATURE_MAP.values().stream()
                .collect(Collectors.groupingBy(f -> f.category));

        Chat.sendMessage(Chat.prefix + " Available features by category:");
        for (Map.Entry<String, List<ToggleableFeature>> entry : byCategory.entrySet()) {
            String categoryFeatures = entry.getValue().stream()
                    .map(f -> f.displayName + " (" + String.join("/", f.aliases) + ")")
                    .collect(Collectors.joining(", "));

            Chat.sendMessage("§6" + entry.getKey() + "§r: " + categoryFeatures);
        }
    }
}