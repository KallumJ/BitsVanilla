package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;

public class DiscordCommand extends Command {

    private static final String DISCORD_INVITE = "https://discord.gg/Arfcnku";

    public DiscordCommand() {
        super("discord", new CommandInformation()
                .setDescription("Displays the discord invite to the player")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Text message = Text.literal("Join our discord here: " + DISCORD_INVITE)
                .styled(style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.literal("Click here to join!"))
                        )
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DISCORD_INVITE))
                        .withColor(Colors.POSITIVE)
                );

        context.getSource().getPlayer().sendMessage(message);

        return 1;
    }
}
