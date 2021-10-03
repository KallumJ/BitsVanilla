package team.bits.vanilla.fabric.statistics.lib;

import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.utils.Colors;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.vanilla.fabric.database.player.PlayerUtils;
import team.bits.vanilla.fabric.statistics.loot.LootUtils;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

public class StatTracker implements Runnable {

    @Override
    public void run() {
        for (ServerPlayerEntity player : ServerInstance.getOnlinePlayers()) {
            for (TrackedStat trackedStat : CustomStats.TRACKED_STATS.values()) {
                Stat<?> stat = trackedStat.stat();

                // get this player's current values for this statistic
                int currentLevel = getCurrentLevel(player, stat);
                int currentCount = getCurrentCount(player, stat);
                // get this player's stored values for this statistic
                StatRecord storedRecord = getStoredRecord(player, stat);

                // check if either the level or the count increased
                if (currentCount > storedRecord.count() || currentLevel > storedRecord.level()) {

                    // create an updated record and store it in the player data
                    StatRecord updatedRecord = new StatRecord(currentLevel, currentCount);
                    setStoredRecord(player, stat, updatedRecord);

                    // enqueue an update to the player API
                    PlayerAPIStatsSyncHandler.enqueue(trackedStat, player, updatedRecord);

                    // check if the player levelled up
                    if (currentLevel > storedRecord.level()) {

                        // play a sound and send a message
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

                        // give the player their reward
                        ((ExtendedPlayerEntity) player).giveItems(LootUtils.getLoot(player));
                    }
                }
            }
        }
    }

    public static @NotNull StatRecord getStoredRecord(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat) {
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        Identifier statId = getStatId(stat);
        return ePlayer.getStatRecord(statId);
    }

    public static void setStoredRecord(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat, @NotNull StatRecord record) {
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        Identifier statId = getStatId(stat);
        ePlayer.setStatRecord(statId, record);
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

    public static int getCurrentCount(@NotNull ServerPlayerEntity player, @NotNull Stat<?> stat) {
        return player.getStatHandler().getStat(stat);
    }

    private static <T> Identifier getStatId(@NotNull Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }
}
