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
                                .then(CommandManager.argument("player", StringArgumentType.string())
                                        .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                        .executes(this)
                                ))

                        .then(literal("del")
                                .requires(source -> source.hasPermissionLevel(4)) // requires permission level 4
                                .then(CommandManager.argument("player", StringArgumentType.string())
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
        Optional<ServerPlayerEntity> player = PlayerUtils.getPlayer(playerArg);
        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();

        if (player.isPresent()) {
            updateVip(true, player.get(), requestingPlayer);
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(SET_ERR, playerArg)).create();
        }

        return 1;
    }

    public int delVip(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        // Get the player whose status is being updated, and update their status
        String playerArg = context.getArgument("player", String.class);
        Optional<ServerPlayerEntity> player = PlayerUtils.getPlayer(playerArg);
        ServerPlayerEntity requestingPlayer = context.getSource().getPlayer();

        if (player.isPresent()) {
           updateVip(false, player.get(), requestingPlayer);
        } else {
            throw new SimpleCommandExceptionType(() -> String.format(DEL_ERR, playerArg)).create();
        }

        return 1;
    }

    private void updateVip(boolean vipStatus, ServerPlayerEntity playerToChange, ServerPlayerEntity requestingPlayer) {

        // Update the passed player with the passed status, and inform the requesting player
        PlayerDataHandle playerDataHandle = PlayerDataHandle.get(playerToChange);

        playerDataHandle.setVip(vipStatus);
        playerDataHandle.save();

        BitsVanilla.audience(requestingPlayer).sendMessage(
                Component.text(String.format(UPDATE_SUCCESS, playerToChange.getEntityName()), Colors.POSITIVE)
        );

    }

}
