package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import unblonded.packets.cfg;
import unblonded.packets.util.Chat;
import unblonded.packets.util.CmdManager.Command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class ToggleCommand extends Command {

    private static final String[] FEATURES = {
            // Combat
            "autocrystal", "crystal",
            "autototem", "totem",
            "autoanchor", "anchor",
            "aimassist", "aim",
            "crystalspam",
            "selfcrystal",
            "totemnotifier", "notifier",
            // Visuals
            "fontsize", "font",
            "playerlist", "players",
            "advesp", "esp",
            "crosshair", "cosmic",
            "handrender", "hand",
            "norender",
            "timechanger", "time",
            // Utility
            "cancelinteraction",
            "autodc", "disconnect",
            "autosell", "sell",
            "chatfilter", "chat",
            "storagescan", "storage",
            "fpschart", "fps",
            // Mining
            "digsafety", "safety",
            "seedray", "oresim"
    };

    // Suggestion provider for autocomplete
    private static final SuggestionProvider<FabricClientCommandSource> FEATURE_SUGGESTIONS =
            (context, builder) -> CommandSource.suggestMatching(FEATURES, builder);

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
                    return 1;
                });
    }

    private int toggleFeature(CommandContext<FabricClientCommandSource> context) {
        String feature = StringArgumentType.getString(context, "feature").toLowerCase();
        boolean toggled = false;
        String featureName = "";

        // Combat features
        switch (feature) {
            case "autocrystal":
            case "crystal":
                cfg.autoCrystal.set(!cfg.autoCrystal.get());
                toggled = cfg.autoCrystal.get();
                featureName = "Auto Crystal";
                break;
            case "autototem":
            case "totem":
                cfg.autoTotem.set(!cfg.autoTotem.get());
                toggled = cfg.autoTotem.get();
                featureName = "Auto Totem";
                break;
            case "autoanchor":
            case "anchor":
                cfg.autoAnchor.set(!cfg.autoAnchor.get());
                toggled = cfg.autoAnchor.get();
                featureName = "Auto Anchor";
                break;
            case "aimassist":
            case "aim":
                cfg.aimAssistToggle.set(!cfg.aimAssistToggle.get());
                toggled = cfg.aimAssistToggle.get();
                featureName = "Aim Assist";
                break;
            case "crystalspam":
                cfg.crystalSpam.set(!cfg.crystalSpam.get());
                toggled = cfg.crystalSpam.get();
                featureName = "Crystal Spam";
                break;
            case "selfcrystal":
                cfg.selfCrystal.set(!cfg.selfCrystal.get());
                toggled = cfg.selfCrystal.get();
                featureName = "Self Crystal";
                break;
            case "totemnotifier":
            case "notifier":
                cfg.totemNotifier.set(!cfg.totemNotifier.get());
                toggled = cfg.totemNotifier.get();
                featureName = "Totem Notifier";
                break;

            // Visual features
            case "fontsize":
            case "font":
                cfg.fontSizeOverride.set(!cfg.fontSizeOverride.get());
                toggled = cfg.fontSizeOverride.get();
                featureName = "Font Size Override";
                break;
            case "playerlist":
            case "players":
                cfg.displayPlayers.set(!cfg.displayPlayers.get());
                toggled = cfg.displayPlayers.get();
                featureName = "Show Player List";
                break;
            case "advesp":
            case "esp":
                cfg.advEsp.set(!cfg.advEsp.get());
                toggled = cfg.advEsp.get();
                featureName = "Advanced ESP";
                break;
            case "crosshair":
            case "cosmic":
                cfg.nightFx.set(!cfg.nightFx.get());
                toggled = cfg.nightFx.get();
                featureName = "Cosmic Crosshair";
                break;
            case "handrender":
            case "hand":
                cfg.handRender.set(!cfg.handRender.get());
                toggled = cfg.handRender.get();
                featureName = "Hand Render";
                break;
            case "norender":
                cfg.noRender.set(!cfg.noRender.get());
                toggled = cfg.noRender.get();
                featureName = "No Render";
                break;
            case "timechanger":
            case "time":
                cfg.timeChanger.set(!cfg.timeChanger.get());
                toggled = cfg.timeChanger.get();
                featureName = "Time Changer";
                break;

            // Utility features
            case "cancelinteraction":
                cfg.cancelInteraction.set(!cfg.cancelInteraction.get());
                toggled = cfg.cancelInteraction.get();
                featureName = "Interaction Canceler";
                break;
            case "autodc":
            case "disconnect":
                cfg.autoDc.set(!cfg.autoDc.get());
                toggled = cfg.autoDc.get();
                featureName = "Auto Disconnect";
                break;
            case "autosell":
            case "sell":
                cfg.autoSell.set(!cfg.autoSell.get());
                toggled = cfg.autoSell.get();
                featureName = "Auto Sell";
                break;
            case "chatfilter":
            case "chat":
                cfg.chatFilter.set(!cfg.chatFilter.get());
                toggled = cfg.chatFilter.get();
                featureName = "Chat Filter";
                break;
            case "storagescan":
            case "storage":
                cfg.storageScan.set(!cfg.storageScan.get());
                toggled = cfg.storageScan.get();
                featureName = "Storage Scan";
                break;
            case "fpschart":
            case "fps":
                cfg.showFpsChart.set(!cfg.showFpsChart.get());
                toggled = cfg.showFpsChart.get();
                featureName = "FPS Chart";
                break;

            // Mining features
            case "digsafety":
            case "safety":
                cfg.checkPlayerAirSafety.set(!cfg.checkPlayerAirSafety.get());
                toggled = cfg.checkPlayerAirSafety.get();
                featureName = "Player Dig Safety";
                break;
            case "seedray":
            case "oresim":
                cfg.oreSim.set(!cfg.oreSim.get());
                toggled = cfg.oreSim.get();
                featureName = "Seed-Ray";
                break;

            default:
                Chat.sendMessage(Chat.prefix + " §cUnknown feature: " + feature);
                return 0;
        }

        String status = toggled ? "§aenabled" : "§cdisabled";
        Chat.sendMessage(Chat.prefix + " §e" + featureName + " " + status);
        return 1;
    }
}