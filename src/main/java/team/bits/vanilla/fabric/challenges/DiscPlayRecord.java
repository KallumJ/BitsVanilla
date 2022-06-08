package team.bits.vanilla.fabric.challenges;

import net.minecraft.item.*;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.*;

public record DiscPlayRecord(@NotNull ServerPlayerEntity player, @NotNull MusicDiscItem disc, long startTime) {
}

