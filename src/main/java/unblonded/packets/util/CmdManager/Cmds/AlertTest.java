package unblonded.packets.util.CmdManager.Cmds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import unblonded.packets.imgui.Alert;
import unblonded.packets.util.CmdManager.Command;

public class AlertTest extends Command {
    public AlertTest() {
        super("alert", "Test command for alerts");
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.executes(context -> {
            Alert.warning("Tested Alert", "Hello Sir");
            return 1;
        });
    }
}
