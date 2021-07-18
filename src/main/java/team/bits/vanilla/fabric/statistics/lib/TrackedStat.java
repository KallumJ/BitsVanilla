package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.stat.Stat;

public record TrackedStat(Stat<?> stat, String customName, int[] levelCounts, String levelupMessage) {
}
