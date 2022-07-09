package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

public class TogglePVPCommand extends Command {
    private static final String ENABLED_MSG = "PVP is now enabled. Other pvpers can now damage you.";
    private static final String DISABLED_MSG = "PVP is now disabled. Other pvpers can no longer damage you.";

    public TogglePVPCommand() {
        super("togglepvp", new CommandInformation()
                .setDescription("Toggle's whether PVP is enabled for you")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();
        final ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;

        boolean pvp = ePlayer.hasPvpEnabled();

        ePlayer.setPvpEnabled(!pvp);
        if (pvp) {
            player.sendMessage(Text.literal(DISABLED_MSG).styled(style -> style.withColor(Formatting.GREEN)));
        } else {
            player.sendMessage(Text.literal(ENABLED_MSG).styled(style -> style.withColor(Formatting.GREEN)));
        }
        return 1;
    }
}
