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
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.utils.Colors;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

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
            Optional<ServerPlayerEntity> foundPlayer = PlayerUtils.getPlayer(playerName);
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

        BitsVanilla.audience(source).sendMessage(Component.text(String.format(PLAYTIME, PlayerUtils.getEffectiveName(player), hours)).color(Colors.NEUTRAL));

        return 1;
    }
}
