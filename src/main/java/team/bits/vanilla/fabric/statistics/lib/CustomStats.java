package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.stat.Stat;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @see net.minecraft.stat.Stats
 */
public final class CustomStats {

    private CustomStats() {
    }

    public static final Map<Stat<?>, TrackedStat> TRACKED_STATS = new HashMap<>();

    public static final Identifier NETHER_PORTALS_USED = register(
            "nether_portals_used", StatFormatter.DEFAULT, new int[]{50, 200, 500},
            "%user% must feel dizzy after all that inter-dimensionary travel... They have used a " +
                    "nether portal %count% times and leveled up their nether portalling skill to %level%!"
    );

    private static @NotNull Identifier register(@NotNull String id, @NotNull StatFormatter formatter,
                                                int[] levelCounts, String levelupMessage) {
        // all our custom stats need to be in the 'bits' namespace
        String namespacedId = String.format("bits:%s", id);
        // create the identifier for this stat
        Identifier identifier = new Identifier(namespacedId);
        // register the identifier
        Registry.register(Registry.CUSTOM_STAT, namespacedId, identifier);
        // create the stat so we can use it
        Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(identifier, formatter);
        // register the stat as a tracked stat
        TRACKED_STATS.put(stat, new TrackedStat(stat, levelCounts, levelupMessage));
        // return the identifier for this stat
        return identifier;
    }
}
