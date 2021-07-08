package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

/**
 * @see net.minecraft.stat.Stats
 */
public final class CustomStats {

    private CustomStats() {
    }

    public static final Identifier NETHER_PORTALS_USED = register("nether_portals_used", StatFormatter.DEFAULT);

    private static @NotNull Identifier register(@NotNull String id, @NotNull StatFormatter formatter) {
        // all our custom stats need to be in the 'bits' namespace
        String namespacedId = String.format("bits:%s", id);
        // create the identifier for this stat
        Identifier identifier = new Identifier(namespacedId);
        // register the identifier
        Registry.register(Registry.CUSTOM_STAT, namespacedId, identifier);
        // create the stat so we can use it
        Stats.CUSTOM.getOrCreateStat(identifier, formatter);
        // return the identifier for this stat
        return identifier;
    }
}
