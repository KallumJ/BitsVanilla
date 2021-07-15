package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.stat.Stat;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.StatType;
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

    public static final Identifier BEDS_EXPLODED = register(
            "beds_exploded", StatFormatter.DEFAULT, new int[]{5, 10, 500},//new int[]{5, 50, 500}
            "%user% is having trouble sleeping. They have blown up %count% beds! They have leveled up their bed demolition skills to %level%"
    );

    public static final Identifier COPPER_WAXED = register(
            "copper_waxed", StatFormatter.DEFAULT, new int[]{5, 10, 15},
            "%user% hates oxidation. They have waxed %count% copper blocks, and levelled up their waxing copper skill to %level%"
    );

    static {
        registerVanillaStat(
                Stats.CUSTOM, Stats.EAT_CAKE_SLICE, new int[]{1, 2, 3},
                "This is such a cool message it's like magic isn't it (level %level% btw)"
        );
    }

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

    private static <T> void registerVanillaStat(@NotNull StatType<T> statType, @NotNull T statId,
                                                int[] levelCounts, String levelupMessage) {
        // get the stat so we can use it
        Stat<T> stat = statType.getOrCreateStat(statId);
        // return the identifier for this stat
        TRACKED_STATS.put(stat, new TrackedStat(stat, levelCounts, levelupMessage));
    }
}
