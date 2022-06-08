package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import team.bits.nibbles.command.*;
import team.bits.vanilla.fabric.util.*;

public class AFKCommand extends Command {

    public AFKCommand() {
        super("afk", new CommandInformation()
                .setDescription("Sets the players status to AFK")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        AFKManager.makeVisuallyAfk(context.getSource().getPlayer());
        return 1;
    }
}
