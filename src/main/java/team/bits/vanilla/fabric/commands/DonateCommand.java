package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.BitsVanilla;

public class DonateCommand extends Command {
    private static final String DONATE_LINK = "http://bits.team/donate";

    public DonateCommand() {
        super("donate", new CommandInformation()
                .setDescription("Displays the donate link to the player")
                .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TextComponent message = Component.text("Donate here! Thank you very much for your generosity!: " + DONATE_LINK)
                .color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Click here to donate!")))
                .clickEvent(ClickEvent.openUrl(DONATE_LINK));

        BitsVanilla.adventure().audience(context.getSource())
                .sendMessage(message);

        return 1;
    }
}
