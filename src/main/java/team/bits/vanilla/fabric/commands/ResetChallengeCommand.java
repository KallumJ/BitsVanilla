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
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class ResetChallengeCommand extends Command {

    private static final String CHALLENGE_RESET_MSG = "Challenge reset";
    private static final String UNKNOWN_PLAYER_ERR = "Unknown player %s";
    private static final String UNKNOWN_CHALLENGE_ERR = "Unknown challenge %s";

    public ResetChallengeCommand() {
        super("resetchallenge", new CommandInformation()
                .setDescription("Reset a challenge of another player")
                .setPublic(false)
                .setUsage("<challenge> <player>")
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("challenge", StringArgumentType.string())
                                .suggests(CommandSuggestionUtils.CHALLENGES)
                                .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                        .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                        .executes(AsyncCommand.wrap(this::run))
                                )
                        )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();
        final String challengeArg = context.getArgument("challenge", String.class);
        final String playerArg = context.getArgument("player", String.class);

        Optional<ServerPlayerEntity> player = PlayerApiUtils.getPlayer(playerArg);
        if (player.isPresent()) {

            Optional<Challenge> challenge = Challenges.getChallenge(challengeArg);
            if (challenge.isPresent()) {

                ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player.get();

                if (challenge.get().equals(Challenges.WORLD_CORNERS)) {
                    ePlayer.clearVisitedCorners();
                }

                ePlayer.resetCompletedChallenge(challenge.get());

                source.sendFeedback(Text.literal(CHALLENGE_RESET_MSG), false);

            } else {
                source.sendError(Text.literal(UNKNOWN_CHALLENGE_ERR));
            }
        } else {
            source.sendError(Text.literal(UNKNOWN_PLAYER_ERR));
        }

        return 1;
    }
}
