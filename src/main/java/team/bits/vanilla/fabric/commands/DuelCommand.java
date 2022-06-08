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
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.listeners.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class DuelCommand extends Command {

    private static final String PLAYER_NOT_FOUND_ERR = "Player '%s' cannot be found";
    private static final String ALREADY_IN_DUEL_ERR = "You are already in a duel";
    private static final String NO_DUEL_ERR = "You don't have any duel requests";
    private static final String DUEL_REQUEST_MSG = "%s challenged you to a duel. Write /duel accept or click this message to accept it.";
    private static final String DUEL_SENT_MSG = "Duel request sent.";
    private static final String DUEL_ACCEPTED_MSG = "Duel request accepted.";

    private final Map<ServerPlayerEntity, ServerPlayerEntity> duelRequests = new HashMap<>();

    public DuelCommand() {
        super("duel", new CommandInformation()
                .setDescription("Invite another player to a duel")
                .setUsage("<player>")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                .executes(this)
                        )
                        .then(literal("accept")
                                .executes(this::accept)
                        )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();
        final ExtendedPlayerEntity eRequestingPlayer = (ExtendedPlayerEntity) requestingPlayer;

        String targetName = context.getArgument("player", String.class);
        Optional<ServerPlayerEntity> target = PlayerApiUtils.getPlayer(targetName);

        if (target.isPresent() && !target.get().equals(requestingPlayer)) {
            ServerPlayerEntity targetPlayer = target.get();

            if (eRequestingPlayer.getDuelTarget().isEmpty()) {

                this.duelRequests.put(targetPlayer, requestingPlayer);

                String requestingPlayerName = PlayerApiUtils.getEffectiveName(requestingPlayer);
                targetPlayer.sendMessage(Text.literal(String.format(DUEL_REQUEST_MSG, requestingPlayerName))
                                .styled(style -> style
                                        .withColor(Colors.NEUTRAL)
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Text.literal("Click to accept")
                                        ))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/duel accept"
                                        ))
                                ),
                        false
                );
                requestingPlayer.sendMessage(Text.literal(DUEL_SENT_MSG), MessageTypes.NEUTRAL);

            } else {
                throw new SimpleCommandExceptionType(() -> ALREADY_IN_DUEL_ERR).create();
            }
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(PLAYER_NOT_FOUND_ERR, targetName)).create();

        }

        return 1;
    }

    public int accept(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity player = context.getSource().getPlayer();

        if (this.duelRequests.containsKey(player)) {
            ServerPlayerEntity targetPlayer = this.duelRequests.get(player);

            player.sendMessage(Text.literal(DUEL_ACCEPTED_MSG), MessageTypes.POSITIVE);
            targetPlayer.sendMessage(Text.literal(DUEL_ACCEPTED_MSG), MessageTypes.POSITIVE);

            DuelHandler.startDuel(player, targetPlayer);

        } else {
            throw new SimpleCommandExceptionType(() -> NO_DUEL_ERR).create();
        }

        return 1;
    }
}
