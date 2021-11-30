package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.entity.EntityType;
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

    public static final Identifier DRIPLEAFS_TILTED = register(
            "dripleafs_tilted", StatFormatter.DEFAULT, new int[]{10, 200, 5000},
            "%user% has just taken up residence at Tilted Towers! They have tilted %count% dripleaves, and reached level %level%."
    );

    public static final Identifier TIMES_TELEPORTED = register(
            "times_teleported", StatFormatter.DEFAULT, new int[]{1, 500, 5000},
            "%user%? Are you ok? Have all your atoms been been put back to the right place? They have teleported %count% times, and reached level %level%."
    );

    public static final Identifier GLOWBERRIES_FED = register(
            "glowberries_fed", StatFormatter.DEFAULT, new int[]{10, 200, 5000},
            "%user% likes their foxes SHINY! They have fed %count% glowberries to their fox, and reached level %level%."
    );

    public static final Identifier FREEZING_DAMAGE_TAKEN = register(
            "freezing_damage_taken", StatFormatter.DEFAULT, new int[]{25, 250, 2500},
            "%user% is cold as ice! And seemingly willing to sacrifice their LIFE!. They have taken %count% points of freezing damage, and reached level %level%."
    );

    public static final Identifier PIGS_CONVERTED = register(
            "pigs_converted", StatFormatter.DEFAULT, new int[]{10, 50, 2500},
            "Dr Franken%user% has been doing some monster experiments! They have converted %count% pigs into Piglins, reaching level %level%."
    );

    public static final Identifier CANDLES_LIT_WITH_SNOWBALLS = register(
            "candles_lit_with_snowballs", StatFormatter.DEFAULT, new int[]{2, 100, 5000},
            "%user% doesn't play by the rules, and they will light their candles how they like. They have lit %count% candles with a snowball... reaching level %level%."
    );

    public static final Identifier KELP_TRIMMED = register(
            "kelp_sheared", StatFormatter.DEFAULT, new int[]{50, 1000, 10000},
            "%user% has been giving their bush a trim! They have trimmed %count% kelp, reaching level %level%."
    );

    public static final Identifier CANDLE_CAKES_MADE = register(
            "candle_cakes_made", StatFormatter.DEFAULT, new int[]{10, 200, 5000},
            "It must be %user%'s birthday, and they must be OLDDDDD. They have added %count% candles to their cake, reaching level %level%."
    );

    public static final Identifier GLOW_INK_REMOVED = register(
            "glow_ink_removed", StatFormatter.DEFAULT, new int[]{5, 200, 5000},
            "%user% has gone dark mode. They have removed %count% glow ink sac's from their signs, reaching level %level%."
    );

    static {
        registerVanillaStat(Stats.KILLED, EntityType.GLOW_SQUID, "glow_squids_killed", new int[]{5, 200, 5000},
                "%user% is a heartless monster and likes to murder cuties. They have murdered %count% Glow Squids, and reached level %level%. Press F to pay respects."
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
