package team.bits.vanilla.fabric.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bits.nibbles.player.INibblesPlayer;
import team.bits.vanilla.fabric.statistics.lib.StatRecord;

import java.util.Optional;

/**
 * Interface with custom methods added to the {@link net.minecraft.entity.player.PlayerEntity} class.
 * In order to use it, simply cast a {@link net.minecraft.entity.player.PlayerEntity} to {@link ExtendedPlayerEntity}
 */
public interface ExtendedPlayerEntity extends INibblesPlayer {

    long getLastRTPTime();

    void setLastRTPTime(long time);

    Optional<PlayerEntity> getDuelTarget();

    void setDuelTarget(@Nullable PlayerEntity player);

    @NotNull StatRecord getStatRecord(@NotNull Identifier statId);

    void setStatRecord(@NotNull Identifier statId, @NotNull StatRecord statRecord);

    boolean hasMigratedStats();

    void markMigratedStats();

    void setCustomClient(boolean customClient);

    boolean isCustomClient();

    void setSendTPS(boolean sendTPS);

    boolean shouldSendTPS();

    void setAFK(boolean afk);

    long getTimePlayed();
}
