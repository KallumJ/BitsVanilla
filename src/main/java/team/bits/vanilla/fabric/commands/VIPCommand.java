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
import team.bits.vanilla.fabric.util.*;

import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class VIPCommand extends Command {

    private static final String UPDATE_SUCCESS = "Successfully updated %s's VIP status";
    private static final String SET_ERR = "Failed to make %s a VIP. Are they online?";
    private static final String DEL_ERR = "Failed to revoke %s's VIP status. Are they online?";

    public VIPCommand() {
        super("vip", new CommandInformation()
                .setDescription("Sets the player to a vip")
                .setPublic(false)
                .setUsage("set <player>")
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .then(literal("set")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                        .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                        .executes(this)
                                ))

                        .then(literal("del")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("player", StringArgumentType.greedyString())
                                        .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                        .executes(this::delVip)
                                )
                        ));


        super.registerAliases(dispatcher, commandNode);
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        // Get the player whose status is being updated, and update their status
        String playerArg = context.getArgument("player", String.class);
        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();

        if (updateVip(true, playerArg, requestingPlayer)) {
            return 1;
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(SET_ERR, playerArg)).create();
        }
    }

    public int delVip(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        // Get the player whose status is being updated, and update their status
        String playerArg = context.getArgument("player", String.class);
        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();

        if (updateVip(false, playerArg, requestingPlayer)) {
            return 1;
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(DEL_ERR, playerArg)).create();
        }
    }

    /**
     * A method to update the VIP status of the player with the passed name
     *
     * @param vipStatus        true if vip, false otherwise
     * @param playerArg        player's username/nickname
     * @param requestingPlayer the player making the change
     * @return true if succeeded, false if failed.
     */
    private boolean updateVip(boolean vipStatus, String playerArg, ServerPlayerEntity requestingPlayer) {
        Optional<ServerPlayerEntity> playerToChange = PlayerApiUtils.getPlayer(playerArg);

        if (playerToChange.isPresent()) {
            ServerPlayerEntity player = playerToChange.get();

            // Update the passed player with the passed status, and inform the requesting player
            PlayerApiUtils.setVIP(player, vipStatus);

            requestingPlayer.sendMessage(Text.literal(String.format(UPDATE_SUCCESS, player.getEntityName())).styled(style -> style.withColor(Colors.POSITIVE)));

            return true;
        } else {
            return false;
        }

    }


}
