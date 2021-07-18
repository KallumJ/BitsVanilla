package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public final class StatUtils {

    private StatUtils() {
    }

    /**
     * Increment a stat value for a player
     *
     * @param player         player to increment the stat for
     * @param statIdentifier identifier of the stat to increment
     * @param amount         amount to increment by
     */
    public static void incrementStat(@NotNull ServerPlayerEntity player, @NotNull Identifier statIdentifier, int amount) {
        final StatHandler statHandler = player.getStatHandler();
        final Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(statIdentifier);
        statHandler.increaseStat(player, stat, amount);
    }

    public static Collection<StatisticRecord> getStats(@NotNull ServerPlayerEntity player) {
        Collection<StatisticRecord> stats = new LinkedList<>();
        for (TrackedStat stat : CustomStats.TRACKED_STATS.values()) {
            int level = StatTracker.getStoredLevel(player, stat.stat());
            if (level > 0) {
                int count = StatTracker.getCurrentCount(player, stat.stat());
                stats.add(new StatisticRecord(stat, count, level));
            }
        }
        return Collections.unmodifiableCollection(stats);
    }

    public static record StatisticRecord(@NotNull TrackedStat stat, int count, int level) {
    }
}
