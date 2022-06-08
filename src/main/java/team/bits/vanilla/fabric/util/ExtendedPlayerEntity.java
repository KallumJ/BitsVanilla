package team.bits.vanilla.fabric.util;

import net.minecraft.entity.player.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.player.*;
import team.bits.vanilla.fabric.challenges.*;

import java.util.*;

/**
 * Interface with custom methods added to the {@link net.minecraft.entity.player.PlayerEntity} class.
 * In order to use it, simply cast a {@link net.minecraft.entity.player.PlayerEntity} to {@link ExtendedPlayerEntity}
 */
public interface ExtendedPlayerEntity extends INibblesPlayer {

    long getLastRTPTime();

    void setLastRTPTime(long time);

    Optional<PlayerEntity> getDuelTarget();

    void setDuelTarget(@Nullable PlayerEntity player);

    void setCustomClient(boolean customClient);

    boolean isCustomClient();

    void setSendTPS(boolean sendTPS);

    boolean shouldSendTPS();

    void setAFK(boolean afk);

    long getTimePlayed();

    boolean hasCompletedChallenge(@NotNull Challenge challenge);

    void markChallengeCompleted(@NotNull Challenge challenge);

    boolean hasVisitedCorner(@NotNull WorldCorner corner);

    void markVisitedCorner(@NotNull WorldCorner corner);
}
