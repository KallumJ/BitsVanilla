package team.bits.vanilla.fabric.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Interface with custom methods added to the {@link net.minecraft.entity.player.PlayerEntity} class.
 * In order to use it, simply cast a {@link net.minecraft.entity.player.PlayerEntity} to {@link ExtendedPlayerEntity}
 */
public interface ExtendedPlayerEntity {

    long getLastRTPTime();

    void setLastRTPTime(long time);

    boolean hasPlayedBefore();

    void giveItem(@NotNull ItemStack itemStack);

    boolean hasItem(@NotNull Item item, int amount);

    boolean removeItem(@NotNull Item item, int amount);
}
