package team.bits.vanilla.fabric.teleport;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.teleport.Location;

import java.util.Objects;

public class Teleport {

    private final PlayerEntity player;
    private final Location destination;

    private int cooldown;

    public Teleport(@NotNull PlayerEntity player, @NotNull Location destination, int cooldown) {
        this.player = Objects.requireNonNull(player);
        this.destination = Objects.requireNonNull(destination);
        this.cooldown = cooldown;
    }

    public @NotNull PlayerEntity getPlayer() {
        return this.player;
    }

    public @NotNull Location getDestination() {
        return this.destination;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
