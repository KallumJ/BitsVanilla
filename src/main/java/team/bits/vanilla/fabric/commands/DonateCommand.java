package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;

public class DonateCommand extends Command {

    private static final String DONATE_LINK = "https://bits.team/donate";

    public DonateCommand() {
        super("donate", new CommandInformation()
                .setDescription("Displays the donate link to the player")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            Text message = Text.literal("Donate here! Thank you very much for your generosity!: " + DONATE_LINK)
                    .styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("Click here to donate!"))
                            )
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DONATE_LINK))
                    );

            player.sendMessage(message, MessageTypes.POSITIVE);
        }

        return 1;
    }
}
