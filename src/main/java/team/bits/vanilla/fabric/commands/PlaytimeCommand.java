package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.tree.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class PlaytimeCommand extends Command {

    private static final String NO_PLAYER_ERR = "There is no player %s online";
    private static final String PLAYTIME = "Player %s has played for %s hours";

    public PlaytimeCommand() {
        super("playtime", new CommandInformation()
                .setDescription("See the active playtime of a player")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .executes(this)
                        .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                .executes(this)
                        )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();

        ServerPlayerEntity player;
        try {
            String playerName = context.getArgument("player", String.class);
            Optional<ServerPlayerEntity> foundPlayer = PlayerApiUtils.getPlayer(playerName);
            if (foundPlayer.isPresent()) {
                player = foundPlayer.get();
            } else {
                throw new SimpleCommandExceptionType(() -> String.format(NO_PLAYER_ERR, playerName)).create();
            }
        } catch (IllegalArgumentException ex) {
            player = context.getSource().getPlayer();
        }

        long playtime = ((ExtendedPlayerEntity) player).getTimePlayed();
        int hours = (int) Math.round(((playtime / 20.0) / 60.0) / 60.0);

        source.sendFeedback(
                Text.literal(String.format(PLAYTIME, PlayerApiUtils.getEffectiveName(player), hours))
                        .styled(style -> style.withColor(Colors.NEUTRAL)),
                false
        );

        return 1;
    }
}
