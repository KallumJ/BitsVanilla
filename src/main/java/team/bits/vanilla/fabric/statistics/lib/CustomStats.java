package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.item.Items;
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
            "beds_exploded", StatFormatter.DEFAULT, new int[]{5, 50, 500},
            "%user% is having trouble sleeping. They have blown up %count% beds! They have leveled up their bed demolition skills to %level%"
    );

    public static final Identifier COPPER_WAXED = register(
            "copper_waxed", StatFormatter.DEFAULT, new int[]{10, 200, 1000},
            "%user% hates oxidation. They have waxed %count% copper blocks, and levelled up their waxing copper skill to %level%"
    );

    static {
        registerVanillaStat(
                Stats.CUSTOM, Stats.EAT_CAKE_SLICE, "cake_eaten", new int[]{20, 200, 2000},
                "%user% has eaten a total of %count% slices of cake! Bob can barely keep up with the amount of cake they're eating! They're now a level %level% cake eater!"
        );

        registerVanillaStat(Stats.CUSTOM, Stats.JUMP, "times_jumped", new int[]{1000, 10000, 75000},
                "%user% must have fitted springs to their shoes! They have jumped %count% times! They're now jumping level %level%!"
        );

        registerVanillaStat(Stats.CUSTOM, Stats.AVIATE_ONE_CM, "distance_flown", new int[]{1000000, 10000000, 50000000},
                "%user% has narrowly avoided experiencing kinetic energy and has flown a whopping %count%! They leveled up their Elytra flying skills to %level%!"
        );

        registerVanillaStat(Stats.CRAFTED, Items.BEACON, "beacons_crafted", new int[]{2, 8, 24},
                "How many status effects can one person need! %user% crafted a total of %count% beacons. They're now a level %level% beacon crafter!"
        );

        registerVanillaStat(Stats.CUSTOM, Stats.DEATHS, "deaths", new int[]{2, 25, 250},
                "%user% pulled a Kallum and died %count% times. They've leveled up their dying skills to %level%"
        );

        registerVanillaStat(Stats.CUSTOM, Stats.TARGET_HIT, "targets_hit", new int[]{10, 200, 2500},
                "Call %user% Legolas because they are one sharp shooter. They have hit %count% targets, and leveled up their target hitting skill to %level%!"
        );

        registerVanillaStat(Stats.CUSTOM, Stats.PIG_ONE_CM, "distance_by_pig", new int[]{1000, 100000, 500000},
                "%user% is a lazy little piggy and has decided to ride a pig to get from A to B! The poor pig has been ridden %count%! %user% leveled up their pig riding skills to %level%!"
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
        TRACKED_STATS.put(stat, new TrackedStat(stat, id, levelCounts, levelupMessage));
        // return the identifier for this stat
        return identifier;
    }

    private static <T> void registerVanillaStat(@NotNull StatType<T> statType, @NotNull T statId, @NotNull String customName,
                                                int[] levelCounts, String levelupMessage) {
        // get the stat so we can use it
        Stat<T> stat = statType.getOrCreateStat(statId);

        // return the identifier for this stat
        TRACKED_STATS.put(stat, new TrackedStat(stat, customName, levelCounts, levelupMessage));
    }
}
