package unblonded.packets.util.CmdManager;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public abstract class Command {
    public final String name;
    public final String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Command(String name) {
        this(name, "No description");
    }

    public abstract void build(LiteralArgumentBuilder<FabricClientCommandSource> builder);
}