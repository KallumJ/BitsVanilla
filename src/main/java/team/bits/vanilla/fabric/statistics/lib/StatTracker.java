package team.bits.vanilla.fabric.statistics.lib;

import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.statistics.loot.LootUtils;
import team.bits.vanilla.fabric.util.Colors;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;
import team.bits.vanilla.fabric.util.ServerInstance;

public class StatTracker implements Runnable {

    @Override
    public void run() {
        for (ServerPlayerEntity player : ServerInstance.getOnlinePlayers()) {
            for (TrackedStat trackedStat : CustomStats.TRACKED_STATS.values()) {
                Stat<?> stat = trackedStat.stat();
                int currentLevel = getCurrentLevel(player, stat);
                int storedLevel = getStoredLevel(player, stat);
                if (currentLevel > storedLevel) {
                    setStoredLevel(player, stat, currentLevel);

                    player.getServerWorld().playSound(
                            player, player.getBlockPos(),
                            SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                            1.0f, 1.0f
                    );

                    int[] levelCounts = trackedStat.levelCounts();

                    ServerInstance.broadcast(Component.text(
                            trackedStat.levelupMessage()
                                    .replace("%user%", PlayerUtils.getEffectiveName(player))
                                    .replace("%count%", stat.format(levelCounts[currentLevel - 1]))
                                    .replace("%level%", String.valueOf(currentLevel)),
                            Colors.POSITIVE
                    ));

                    ((ExtendedPlayerEntity) player).giveItems(LootUtils.getLoot(player));
                }
            }
        }
    }

    private static int getStoredLevel(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat) {
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        Identifier statId = getStatId(stat);
        return ePlayer.getStatLevel(statId);
    }

    private static void setStoredLevel(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat, int level) {
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        Identifier statId = getStatId(stat);
        ePlayer.setStatLevel(statId, level);
    }

    private static int getCurrentLevel(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat) {
        int currentCount = getCurrentCount(player, stat);
        if (currentCount == 0) {
            return 0;
        }

        TrackedStat trackedStat = CustomStats.TRACKED_STATS.get(stat);
        int[] levelCounts = trackedStat.levelCounts();

        for (int i = 0; i < levelCounts.length; i++) {
            int level = levelCounts.length - (i + 1);
            if (currentCount >= levelCounts[level]) {
                return level + 1;
            }
        }

        return 0;
    }

    private static int getCurrentCount(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat) {
        return player.getStatHandler().getStat(stat);
    }

    private static <T> Identifier getStatId(@NotNull Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }
}
