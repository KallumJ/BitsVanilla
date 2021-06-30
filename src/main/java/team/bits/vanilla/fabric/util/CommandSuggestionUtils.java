package team.bits.vanilla.fabric.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestionUtils {
    public static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS;

    static {
        ONLINE_PLAYERS = (context, builder) -> {
            ServerWrapper serverWrapper = new ServerWrapper(context.getSource().getMinecraftServer());

            List<String> playersList = serverWrapper.getOnlinePlayersNames();

            return filterSuggestionsByInput(builder, playersList);
        };
    }

    private static CompletableFuture<Suggestions> filterSuggestionsByInput(SuggestionsBuilder builder, List<String> values) {
        String start = builder.getRemaining().toLowerCase();
        values.stream().filter(s -> s.toLowerCase().startsWith(start)).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
