package unblonded.packets.util.CmdManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    public static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private static final List<Command> commands = new ArrayList<>();
    private static final String PREFIX = "`";

    public static void init() {
        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            if (message.startsWith(PREFIX)) {
                handleCommand(message);
                return false;
            }
            return true;
        });
    }

    public static void register(Command command) {
        commands.add(command);

        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(command.name);
        command.build(builder);
        DISPATCHER.register(builder);
    }

    private static void handleCommand(String message) {
        try {
            String commandText = message.substring(PREFIX.length());
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.getNetworkHandler() != null) {
                DISPATCHER.execute(commandText, client.getNetworkHandler().getCommandSource());
            }
        } catch (Exception e) {sendMessage("Error executing command: " + e.getMessage());}
    }

    private static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    public static String getPrefix() {return PREFIX;}

    public static List<Command> getCommands() {return new ArrayList<>(commands);}
}