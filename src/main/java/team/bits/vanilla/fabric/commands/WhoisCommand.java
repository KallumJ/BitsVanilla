package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class WhoisCommand extends AsyncCommand {

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
    public void runAsync(@NotNull CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final String nickname = context.getArgument("nickname", String.class);

        Optional<String> username = PlayerApiUtils.getUsernameForNickname(nickname);
        if (username.isPresent()) {
            source.sendFeedback(
                    Text.literal(String.format(WHOIS_MESSAGE, nickname, username.get()))
                            .styled(style -> style.withColor(Colors.POSITIVE)),
                    false
            );
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(NOT_FOUND_ERR, nickname)).create();
        }
    }
}
