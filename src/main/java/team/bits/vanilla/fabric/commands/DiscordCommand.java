package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.BitsVanilla;

import static net.minecraft.server.command.CommandManager.literal;

public class DiscordCommand extends Command {

    private static final String DISCORD_INVITE = "https://discord.gg/Arfcnku";

    public DiscordCommand() {
        super("discord");
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
