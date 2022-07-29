package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;

public class MapCommand extends Command {
    private static final String MAP_LINK = "https://bits.team/map";

    public MapCommand() {
        super("webmap", new CommandInformation()
                        .setDescription("Displays the map link to the player")
                        .setPublic(true),
                "wm", "m"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            Text message = Text.literal("The map can be viewed here: " + MAP_LINK)
                    .styled(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.literal("Click here to view the map!"))
                            )
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, MAP_LINK))
                            .withColor(Colors.POSITIVE)
                    );
            player.sendMessage(message);
        }

        return 1;
    }
}
