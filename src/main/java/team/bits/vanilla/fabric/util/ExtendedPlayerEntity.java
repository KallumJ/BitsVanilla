package team.bits.vanilla.fabric.util;

/**
 * Interface with custom methods added to the {@link net.minecraft.entity.player.PlayerEntity} class.
 * In order to use it, simply cast a {@link net.minecraft.entity.player.PlayerEntity} to {@link ExtendedPlayerEntity}
 */
public interface ExtendedPlayerEntity {

    long getLastRTPTime();

    void setLastRTPTime(long time);
}
