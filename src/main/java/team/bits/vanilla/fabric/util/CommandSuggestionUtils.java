package team.bits.vanilla.fabric.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.database.warp.WarpUtils;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestionUtils {
    public static final SuggestionProvider<ServerCommandSource> ONLINE_PLAYERS;
    public static final SuggestionProvider<ServerCommandSource> ALL_PLAYERS;
    public static final SuggestionProvider<ServerCommandSource> WARPS;

    static {
        ONLINE_PLAYERS = (context, builder) -> filterSuggestionsByInput(builder, PlayerUtils.getOnlinePlayerNames());
        ALL_PLAYERS = (context, builder) -> filterSuggestionsByInput(builder, PlayerUtils.getAllNames());
        WARPS = (context, builder) -> filterSuggestionsByInput(builder, WarpUtils.getWarpsList());
    }

    private static CompletableFuture<Suggestions> filterSuggestionsByInput(SuggestionsBuilder builder, Collection<String> values) {
        String start = builder.getRemaining().toLowerCase();
        values.stream().filter(s -> s.toLowerCase().startsWith(start)).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
