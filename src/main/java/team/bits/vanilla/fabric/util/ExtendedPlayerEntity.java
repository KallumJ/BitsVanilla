package team.bits.vanilla.fabric.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bits.nibbles.player.INibblesPlayer;

import java.util.Collection;
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

    int getStatLevel(@NotNull Identifier statId);

    void setStatLevel(@NotNull Identifier statId, int level);

    boolean hasMigratedStats();

    void markMigratedStats();

    void setCustomClient(boolean customClient);

    boolean isCustomClient();

    void setSendTPS(boolean sendTPS);

    boolean shouldSendTPS();
}
