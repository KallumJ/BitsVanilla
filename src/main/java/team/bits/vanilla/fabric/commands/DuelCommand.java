package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.utils.Colors;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.listeners.DuelHandler;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

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
        Optional<ServerPlayerEntity> target = PlayerUtils.getPlayer(targetName);

        if (target.isPresent() && !target.get().equals(requestingPlayer)) {
            ServerPlayerEntity targetPlayer = target.get();

            if (eRequestingPlayer.getDuelTarget().isEmpty()) {

                this.duelRequests.put(targetPlayer, requestingPlayer);

                String requestingPlayerName = PlayerUtils.getEffectiveName(requestingPlayer);
                BitsVanilla.audience(targetPlayer).sendMessage(
                        Component.text(String.format(DUEL_REQUEST_MSG, requestingPlayerName), Colors.NEUTRAL)
                                .hoverEvent(HoverEvent.showText(Component.text("Click to accept")))
                                .clickEvent(ClickEvent.runCommand("/duel accept"))
                );
                BitsVanilla.audience(requestingPlayer).sendMessage(Component.text(DUEL_SENT_MSG, Colors.NEUTRAL));

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

            Component confirmMessage = Component.text(DUEL_ACCEPTED_MSG, Colors.POSITIVE);
            BitsVanilla.audience(player).sendMessage(confirmMessage);
            BitsVanilla.audience(targetPlayer).sendMessage(confirmMessage);

            DuelHandler.startDuel(player, targetPlayer);

        } else {
            throw new SimpleCommandExceptionType(() -> NO_DUEL_ERR).create();
        }

        return 1;
    }
}
