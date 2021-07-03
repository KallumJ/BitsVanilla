package team.bits.vanilla.fabric.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.database.player.PlayerUtils;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestionUtils {
    public static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS;
    public static final SuggestionProvider<ServerCommandSource> ALL_PLAYERS;

    static {
        ONLINE_PLAYERS = (context, builder) -> {
            ServerWrapper serverWrapper = new ServerWrapper(context.getSource().getMinecraftServer());

            Collection<String> playersList = serverWrapper.getOnlinePlayers().stream()
                    .map(PlayerUtils::getEffectiveName)
                    .toList();

            return filterSuggestionsByInput(builder, playersList);
        };

        ALL_PLAYERS = (context, builder) -> filterSuggestionsByInput(builder, PlayerUtils.getAllNames());
    }

    private static CompletableFuture<Suggestions> filterSuggestionsByInput(SuggestionsBuilder builder, Collection<String> values) {
        String start = builder.getRemaining().toLowerCase();
        values.stream().filter(s -> s.toLowerCase().startsWith(start)).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
