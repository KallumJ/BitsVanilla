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
import net.minecraft.util.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class ChallengesCommand extends Command {
    private static final String NO_PLAYER_ERR = "There is no player %s online";
    private static final String HOVER_TXT = "%s - Reward: %s";
    private static final String CHALLENGE_HEADER = "--- %s's Challenges ---\n";

    public ChallengesCommand() {
        super("challenges", new CommandInformation()
                        .setDescription("View the status of yours, or another players challenges")
                        .setPublic(true)
                        .setUsage("[player]"),
                "ch"
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

        dispatcher.register(
                literal("ch")
                        .executes(this)
                        .redirect(commandNode)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();

        ServerPlayerEntity player;
        try {
            String playerName = context.getArgument("player", String.class);
            Optional<ServerPlayerEntity> optPlayer = PlayerApiUtils.getPlayer(playerName);

            if (optPlayer.isPresent()) {
                player = optPlayer.get();
            } else {
                throw new SimpleCommandExceptionType(() -> String.format(NO_PLAYER_ERR, playerName)).create();
            }
        } catch (IllegalArgumentException ex) {
            player = context.getSource().getPlayer();
        }

        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;

        // Get Challenge Texts
        List<Text> challengeComponents = new LinkedList<>();
        for (Challenge challenge : Challenges.getAllChallenges()) {
            challengeComponents.add(constructChallengeText(challenge, ePlayer));
        }

        // Use challenge texts to create and send final message
        source.sendFeedback(constructFinalMessage(challengeComponents, player), false);
        return 1;
    }

    private String getCompletedText(boolean complete) {
        return complete ? "Completed\n" : "Not Completed\n";
    }

    private Text constructFinalMessage(List<Text> challengeComponents, ServerPlayerEntity player) {
        // Create header
        String header = String.format(CHALLENGE_HEADER, PlayerApiUtils.getEffectiveName(player));
        MutableText finalMessage = Text.literal(header).styled(style -> style.withColor(Colors.POSITIVE));

        // Append the challenge texts
        challengeComponents.forEach(finalMessage::append);


        finalMessage.append(Text.literal("*hover to see challenge information*").styled(style -> style.withItalic(true).withColor(Formatting.GRAY)));

        return finalMessage;
    }

    private Text constructChallengeText(Challenge challenge, ExtendedPlayerEntity player) {
        ChallengeInformation info = challenge.getInformation();
        String displayName = info.displayName();
        String challengeString = displayName + ": ";

        // Create main text
        String completedText = getCompletedText(player.hasCompletedChallenge(challenge));
        MutableText challengeStr = Text.literal(challengeString).styled(style -> style.withColor(Colors.NEUTRAL));
        challengeStr.append(Text.literal(completedText).styled(style -> style.withColor(Formatting.WHITE)));

        // Create hover text
        String hoverTxt = String.format(HOVER_TXT, info.description(), info.reward().getName().getString());
        challengeStr.styled(
                style -> style.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hoverTxt))
                )
        );

        return challengeStr;
    }
}
