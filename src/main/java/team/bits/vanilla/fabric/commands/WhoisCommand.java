package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class WhoisCommand extends Command {

    private static final String WHOIS_MESSAGE = "The player behind the nickname \"%s\" is %s";
    private static final String NOT_FOUND_ERR = "There is no player with the nickname \"%s\"";

    public WhoisCommand() {
        super("whois", new CommandInformation()
                .setDescription("See the username of a player")
                .setUsage("<nickname>")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("nickname", StringArgumentType.greedyString())
                                .executes(this)
                                .suggests(CommandSuggestionUtils.NICKNAMES)
                        )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        String nickname = context.getArgument("nickname", String.class);

        Optional<String> username = PlayerUtils.getUsername(nickname);
        if (username.isPresent()) {
            BitsVanilla.audience(source).sendMessage(
                    Component.text(String.format(WHOIS_MESSAGE, nickname, username.get()), Colors.POSITIVE)
            );
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(NOT_FOUND_ERR, nickname)).create();
        }

        return 1;
    }
}
