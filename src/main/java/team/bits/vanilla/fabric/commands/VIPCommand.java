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
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerDataHandle;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

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
        Optional<ServerPlayerEntity> playerToChange = PlayerUtils.getPlayer(playerArg);

        if (playerToChange.isPresent()) {
            ServerPlayerEntity player = playerToChange.get();

            // Update the passed player with the passed status, and inform the requesting player
            PlayerDataHandle playerDataHandle = PlayerDataHandle.get(player);

            playerDataHandle.setVip(vipStatus);
            playerDataHandle.save();


            BitsVanilla.audience(requestingPlayer).sendMessage(
                    Component.text(String.format(UPDATE_SUCCESS, player.getEntityName()), Colors.POSITIVE)
            );

            return true;

        } else {
            return false;
        }

    }


}
