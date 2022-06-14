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
import net.minecraft.world.dimension.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.freecam.Freecam;
import team.bits.vanilla.fabric.teleport.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;
import java.util.concurrent.*;

import static net.minecraft.server.command.CommandManager.*;

public class BeamCommand extends AsyncCommand {

    private static final HashMap<ServerPlayerEntity, Beam> BEAM_REQUESTS = new HashMap<>();
    private static final String REQUEST_STRING = "Beam requested!";
    private static final String ACCEPT_STRING = "%s has requested to beam to you. Click here to accept, or type /beam accept";
    private static final String ACCEPT_ERR = "You have no beam request to accept right now";
    private static final String BEAM_SELF_ERR = "You can not beam to yourself";
    private static final String BEAM_FREECAM_ERR = "You can not beam to someone in freecam";
    private static final String ACCEPT_FREECAM_ERR = "You can not accept a beam while in freecam";
    private static final String SAME_DIM_ERR = "You must be in the same dimension as your target";
    private static final String NO_PLAYER_ERR = "There is no player %s online";
    private static final String NO_ARGS_ERR = "To use /beam, you must specify a player, or do /beam accept. For more info, do /help";
    private static final String TELEPORTS_DISABLED = "You have teleporting disabled!";

    public BeamCommand() {
        super("beam", new CommandInformation()
                .setDescription("Teleports you to the specified player")
                .setUsage("<player>|accept")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Register /beam <player>
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                .executes(AsyncCommand.wrap(this::initialiseBeamRequest))
                        )
        );

        // Register /beam accept
        dispatcher.register(literal(super.getName())
                .then(literal("accept")
                        .executes(AsyncCommand.wrap(this::acceptBeam))
                )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    /**
     * A method to initialise a beam request
     *
     * @param context The command context
     * @return int, command error code
     */
    public void initialiseBeamRequest(CommandContext<ServerCommandSource> context)
            throws ExecutionException, InterruptedException {

        final ServerPlayerEntity sendingPlayer = Objects.requireNonNull(context.getSource().getPlayer());
        final String playerArg = context.getArgument("player", String.class);

        boolean hasTpDisabled = PlayerApiUtils.getHasTPDisabled(sendingPlayer).get();
        if (!hasTpDisabled) {

            Optional<ServerPlayerEntity> receivingPlayer = PlayerApiUtils.getPlayer(playerArg);

            // If a receiving player is found, proceed, else, throw exception
            if (receivingPlayer.isPresent()) {
                DimensionType sendingDimension = sendingPlayer.world.getDimension();
                DimensionType receivingDimension = receivingPlayer.get().world.getDimension();

                // If players are not in the same dimension, throw exception
                if (!sendingDimension.equals(receivingDimension)) {

                    sendingPlayer.sendMessage(Text.literal(SAME_DIM_ERR), MessageTypes.NEGATIVE);

                    // If player is beaming to themselves, throw exception
                } else if (sendingPlayer.equals(receivingPlayer.get())) {
                    sendingPlayer.sendMessage(Text.literal(BEAM_SELF_ERR), MessageTypes.NEGATIVE);
                } else if (Freecam.isPlayerInFreecam(receivingPlayer.get())) {
                    sendingPlayer.sendMessage(Text.literal(BEAM_FREECAM_ERR), MessageTypes.NEGATIVE);
                } else {
                    addBeam(sendingPlayer, receivingPlayer.get());
                }

            } else {
                sendingPlayer.sendMessage(Text.literal(String.format(NO_PLAYER_ERR, playerArg)), MessageTypes.NEGATIVE);
            }

        } else {
            sendingPlayer.sendMessage(Text.literal(TELEPORTS_DISABLED), MessageTypes.NEGATIVE);
        }
    }

    /**
     * A method to accept a beam request
     *
     * @param context The command context
     * @return int, command error code
     * @throws CommandSyntaxException if no beam for this player is active
     */
    private void acceptBeam(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity acceptingPlayer = Objects.requireNonNull(context.getSource().getPlayer());
        final Beam beamRequest = BEAM_REQUESTS.get(acceptingPlayer);

        // If a beam request for this player was found, execute beam, else, throw exception
        if (beamRequest != null) {
            if (!Freecam.isPlayerInFreecam(acceptingPlayer)) {
                beamRequest.executeBeam();
                BEAM_REQUESTS.remove(acceptingPlayer);
            } else {
                throw new SimpleCommandExceptionType(() -> ACCEPT_FREECAM_ERR).create();
            }
        } else {
            throw new SimpleCommandExceptionType(() -> ACCEPT_ERR).create();
        }
    }

    /**
     * A method to add beam request to map of requests
     *
     * @param sendingPlayer   The player making the request
     * @param receivingPlayer The player receiving the request
     */
    private void addBeam(ServerPlayerEntity sendingPlayer, ServerPlayerEntity receivingPlayer) {

        // Create request and add it to the map with receiving player as the key
        BEAM_REQUESTS.put(receivingPlayer, new Beam(sendingPlayer, receivingPlayer));


        // Send success message
        sendingPlayer.sendMessage(Text.literal(REQUEST_STRING), MessageTypes.POSITIVE);

        // Notify receiving player of request
        String sendingPlayerName = PlayerApiUtils.getEffectiveName(sendingPlayer);
        Text acceptMessage = Text.literal(String.format(ACCEPT_STRING, sendingPlayerName))
                .styled(style -> style
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, Text.literal("Click here to accept!")
                        ))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/beam accept"))
                );

        receivingPlayer.sendMessage(acceptMessage, MessageTypes.POSITIVE);
    }

    // Don't think this actually gets used, but its required so. here it is.
    @Override
    public void runAsync(@NotNull CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        throw new SimpleCommandExceptionType(() -> NO_ARGS_ERR).create();
    }
}

record Beam(ServerPlayerEntity sendingPlayer,
            ServerPlayerEntity receivingPlayer) {

    private static final String ACCEPT_MSG = "Beam accepted";

    public void executeBeam() {
        sendingPlayer.sendMessage(Text.literal(ACCEPT_MSG), MessageTypes.NEUTRAL);
        receivingPlayer.sendMessage(Text.literal(ACCEPT_MSG), MessageTypes.NEUTRAL);
        Teleporter.queueTeleport(this.sendingPlayer, Location.get(this.receivingPlayer), false);
    }
}

