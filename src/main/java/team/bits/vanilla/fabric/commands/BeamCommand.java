package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.teleport.Teleporter;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;
import team.bits.vanilla.fabric.util.Location;
import team.bits.vanilla.fabric.util.ServerWrapper;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.literal;

public class BeamCommand extends Command {

    private final static HashMap<ServerPlayerEntity, Beam> BEAM_REQUESTS = new HashMap<>();
    private final static String REQUEST_STRING = "Beam requested!";
    private static final String ACCEPT_STRING = "%s has requested to beam to you. Click here to accept, or type /beam accept";
    private static final String ACCEPT_ERR = "You have no beam request to accept right now";
    private static final String BEAM_SELF_ERR = "You can not beam to yourself";
    private static final String SAME_DIM_ERR = "You must be in the same dimension as your target";
    private static final String NO_PLAYER_ERR = "There is no player %s online";
    private static final String NO_ARGS_ERR = "To use /beam, you must specify a player, or do /beam accept. For more info, do /help";

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
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(literal(super.getName()).then(CommandManager.argument("player", StringArgumentType.string()).suggests(CommandSuggestionUtils.ONLINE_PLAYERS).executes(this::initialiseBeamRequest)));

        // Register /beam accept
        dispatcher.register(literal(super.getName())
                .then(literal("accept")
                        .executes(this::acceptBeam)
                )
        );

        super.registerAliases(dispatcher, commandNode);
    }

    /**
     * A method to initialise a beam request
     *
     * @param context The command context
     * @return int, command error code
     * @throws CommandSyntaxException if sending player is not in the same dimension, they are beaming to themselves, or there is no player by that name online
     */
    public int initialiseBeamRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity sendingPlayer = context.getSource().getPlayer();

        String playerArg = context.getArgument("player", String.class);

        ServerWrapper serverWrapper = new ServerWrapper(context.getSource().getMinecraftServer());
        ServerPlayerEntity receivingPlayer = serverWrapper.getPlayerFromName(playerArg);

        // If a recieving player is found, proceed, else, throw exception
        if (receivingPlayer != null) {
            DimensionType sendingDimension = sendingPlayer.world.getDimension();
            DimensionType receivingDimension = receivingPlayer.world.getDimension();

            // If players are not in the same dimension, throw exception
            if (!sendingDimension.equals(receivingDimension)) {
                throw new SimpleCommandExceptionType(() -> SAME_DIM_ERR).create();
                // If player is beaming to themselves, throw exception
            } else if (sendingPlayer.getEntityName().equals(receivingPlayer.getEntityName())) {
                throw new SimpleCommandExceptionType(() -> BEAM_SELF_ERR).create();
            } else {
                addBeam(sendingPlayer, receivingPlayer);
            }
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(NO_PLAYER_ERR, playerArg)).create();
        }

        return 1;
    }

    /**
     * A method to accept a beam request
     *
     * @param context The command context
     * @return int, command error code
     * @throws CommandSyntaxException if no beam for this player is active
     */
    private int acceptBeam(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity acceptingPlayer = context.getSource().getPlayer();
        Beam beamRequest = BEAM_REQUESTS.get(acceptingPlayer);

        // If a beam request for this player was found, execute beam, else, throw exception
        if (beamRequest != null) {
            beamRequest.executeBeam();
            BEAM_REQUESTS.remove(acceptingPlayer);
        } else {
            throw new SimpleCommandExceptionType(() -> ACCEPT_ERR).create();
        }

        return 1;
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
        TextComponent requestMessage = Component.text(REQUEST_STRING)
                .color(NamedTextColor.GREEN);

        BitsVanilla.adventure().audience(sendingPlayer)
                .sendMessage(requestMessage);

        // Notify receiving player of request
        TextComponent acceptMessage = Component.text(String.format(ACCEPT_STRING, receivingPlayer.getEntityName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click here to accept!")))
                .clickEvent(ClickEvent.runCommand("/beam accept"))
                .color(NamedTextColor.GREEN);

        BitsVanilla.adventure().audience(receivingPlayer)
                .sendMessage(acceptMessage);

    }

    // Don't think this actually gets used, but its required so. here it is.
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        throw new SimpleCommandExceptionType(() -> NO_ARGS_ERR).create();
    }
}

record Beam(ServerPlayerEntity sendingPlayer,
            ServerPlayerEntity receivingPlayer) {

    public void executeBeam() {
        Teleporter.queueTeleport(this.sendingPlayer, Location.get(this.receivingPlayer), null, false);
    }
}

