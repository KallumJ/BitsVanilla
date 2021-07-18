package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.vanilla.fabric.BitsVanilla;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.CommandSuggestionUtils;
import team.bits.vanilla.fabric.util.Utils;

import java.util.Collection;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class StatsCommand extends Command {

    private static final String STATS_HEADER = "%s's Statistics:";
    private static final String STAT_LINE = "%s: %s (Level %s)";
    private static final String NO_PLAYER_ERR = "There is no player %s online";
    private static final String NO_STATS_ERR = "The player %s has no statistics";

    public StatsCommand() {
        super("statistics", new String[]{"stats"}, new CommandInformation()
                .setDescription("See your statistics, or those of another player")
                .setUsage("[player]")
                .setPublic(true)
        );
    }

    @Override
    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal(super.getName())
                        .executes(this)
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(CommandSuggestionUtils.ONLINE_PLAYERS)
                                .executes(this)
                        )
        );

        dispatcher.register(
                literal("stats")
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
            Optional<ServerPlayerEntity> foundPlayer = PlayerUtils.getPlayer(playerName);
            if (foundPlayer.isPresent()) {
                player = foundPlayer.get();
            } else {
                throw new SimpleCommandExceptionType(() -> String.format(NO_PLAYER_ERR, playerName)).create();
            }
        } catch (IllegalArgumentException ex) {
            player = context.getSource().getPlayer();
        }

        Collection<StatUtils.StatisticRecord> stats = StatUtils.getStats(player);

        if (!stats.isEmpty()) {
            Audience audience = BitsVanilla.audience(source);
            audience.sendMessage(
                    Component.text(String.format(STATS_HEADER, PlayerUtils.getEffectiveName(player)), Colors.HEADER)
            );


            for (StatUtils.StatisticRecord record : stats) {
                String name = Utils.fancyFormat(record.stat().customName());
                audience.sendMessage(
                        Component.text(String.format(STAT_LINE, name, record.count(), record.level()), Colors.NEUTRAL)
                );
            }
        } else {
            BitsVanilla.audience(source).sendMessage(Component.text(String.format(NO_STATS_ERR, PlayerUtils.getEffectiveName(player))).color(Colors.NEGATIVE));
        }

        return 1;
    }
}
