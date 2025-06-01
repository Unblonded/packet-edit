package unblonded.packets.util.CmdManager;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

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

    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);
}