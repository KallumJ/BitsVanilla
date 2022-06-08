package team.bits.vanilla.fabric.challenges;

import net.minecraft.server.network.*;

public record WitherDragonRecord(ServerPlayerEntity player, long time, boolean witherKilled, boolean dragonKilled) {
}
