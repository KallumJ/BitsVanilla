package team.bits.vanilla.fabric.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestionUtils {
    public static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS;

    static {
        ONLINE_PLAYERS = (context, builder) -> {
            Server server = new Server(context.getSource().getMinecraftServer());

            List<String> playersList = server.getOnlinePlayersNames();

            return filterSuggestionsByInput(builder, playersList);
        };
    }

    private static CompletableFuture<Suggestions> filterSuggestionsByInput(SuggestionsBuilder builder, List<String> values) {
        String start = builder.getRemaining().toLowerCase();
        values.stream().filter(s -> s.toLowerCase().startsWith(start)).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
