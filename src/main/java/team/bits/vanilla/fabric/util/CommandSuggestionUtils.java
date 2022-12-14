package team.bits.vanilla.fabric.util;

import com.mojang.brigadier.suggestion.*;
import net.minecraft.server.command.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.database.*;

import java.util.*;
import java.util.concurrent.*;

public class CommandSuggestionUtils {

    public static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS;
    public static final SuggestionProvider<ServerCommandSource> ALL_PLAYERS;
    public static final SuggestionProvider<ServerCommandSource> NICKNAMES;
    public static final SuggestionProvider<ServerCommandSource> WARPS;
    public static final SuggestionProvider<ServerCommandSource> CHALLENGES;

    static {
        ONLINE_PLAYERS = (context, builder) -> filterSuggestionsByInputAsync(builder, PlayerApiUtils.getOnlinePlayerNamesAsync());
        ALL_PLAYERS = (context, builder) -> filterSuggestionsByInputAsync(builder, PlayerApiUtils.getAllNamesAsync());
        NICKNAMES = (context, builder) -> filterSuggestionsByInputAsync(builder, PlayerApiUtils.getNicknamesAsync());
        WARPS = (context, builder) -> filterSuggestionsByInputAsync(builder, WarpApiUtils.getWarpsListAsync());
        CHALLENGES = (context, builder) -> filterSuggestionsByInputAsync(builder, CompletableFuture.completedFuture(Challenges.getChallengeNames()));
    }

    private CommandSuggestionUtils() {
    }

    // instead of running the queries for suggestions on the server thread
    // and causing mini lag spikes, we can pass the suggestions in as a
    // future, transform that future into the desired suggestions object
    // and then return it, still as a future. This way we never block
    // the server thread and prevent unneeded lag.
    private static CompletableFuture<Suggestions> filterSuggestionsByInputAsync(SuggestionsBuilder builder,
                                                                                CompletableFuture<Collection<String>> futureSuggestions) {
        final String userInput = builder.getRemaining().toLowerCase();
        return futureSuggestions.thenApply(suggestions -> {
            suggestions.stream()
                    .filter(suggestion -> suggestion.toLowerCase().startsWith(userInput))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
}
