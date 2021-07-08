package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.util.ServerInstance;

public final class StatUtils {

    private StatUtils() {
    }

    /**
     * Get the {@link StatHandler} object for a given player
     *
     * @see PlayerManager#createStatHandler(PlayerEntity)
     */
    public static @NotNull StatHandler getStatHandler(@NotNull ServerPlayerEntity player) {
        final PlayerManager playerManager = ServerInstance.get().getPlayerManager();
        // we can safely use `createStatHandler` because it will only create the object
        // once, and instead return the existing object if it already exists.
        // stat handlers are stored by uuid and access is instant, so it's safe
        // to call this as often as we like
        return playerManager.createStatHandler(player);
    }

    /**
     * Increment a stat value for a player
     *
     * @param player         player to increment the stat for
     * @param statIdentifier identifier of the stat to increment
     * @param amount         amount to increment by
     */
    public static void incrementStat(@NotNull ServerPlayerEntity player, @NotNull Identifier statIdentifier, int amount) {
        final StatHandler statHandler = StatUtils.getStatHandler(player);
        final Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(statIdentifier);
        statHandler.increaseStat(player, stat, amount);
    }
}
