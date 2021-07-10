package team.bits.vanilla.fabric.statistics.lib;

import net.minecraft.stat.Stat;

public record TrackedStat(Stat<?> stat, int[] levelCounts, String levelupMessage) {
}
