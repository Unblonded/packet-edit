package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import unblonded.packets.cheats.Waypoints;
import unblonded.packets.util.CmdManager.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import unblonded.packets.util.Color;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class WaypointCommand extends Command {
    public WaypointCommand() {
        super("w", "Creates a waypoint");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.executes(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            Waypoints.saved.add(new Waypoints.Point(BlockPos.ofFloored(client.player.getPos())));
            return 1;
        });

        builder.then(argument("name", StringArgumentType.greedyString())
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    String name = StringArgumentType.getString(context, "name");
                    Waypoints.saved.add(new Waypoints.Point(BlockPos.ofFloored(client.player.getPos()), name));
                    return 1;
                })
                .then(argument("r", IntegerArgumentType.integer(0, 255))
                        .then(argument("g", IntegerArgumentType.integer(0, 255))
                                .then(argument("b", IntegerArgumentType.integer(0, 255))
                                        .executes(context -> {
                                            MinecraftClient client = MinecraftClient.getInstance();
                                            String name = StringArgumentType.getString(context, "name");
                                            int r = IntegerArgumentType.getInteger(context, "r");
                                            int g = IntegerArgumentType.getInteger(context, "g");
                                            int b = IntegerArgumentType.getInteger(context, "b");

                                            Color color = new Color(r, g, b);
                                            Waypoints.saved.add(new Waypoints.Point(BlockPos.ofFloored(client.player.getPos()), name, color));
                                            return 1;
                                        })))));
    }
}