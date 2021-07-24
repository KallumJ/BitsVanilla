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
        TextComponent message = Component.text("Join our discord here: " + DISCORD_INVITE)
                .color(NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("Click here to join!")))
                .clickEvent(ClickEvent.openUrl(DISCORD_INVITE));

        BitsVanilla.adventure().audience(context.getSource())
                .sendMessage(message);

        return 1;
    }
}
