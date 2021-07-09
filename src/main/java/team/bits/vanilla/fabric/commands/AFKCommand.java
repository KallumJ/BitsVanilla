package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.util.AFKManager;

public class AFKCommand extends Command {
    public AFKCommand() {
        super("afk", new CommandInformation()
                .setDescription("Sets the players status to AFK")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        AFKManager.makePlayerAfk(context.getSource().getPlayer());
        return 1;
    }
}
