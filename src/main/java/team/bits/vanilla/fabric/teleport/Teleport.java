package team.bits.vanilla.fabric.teleport;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.util.Location;

import java.util.Objects;

public class Teleport {

    private final PlayerEntity player;
    private final Location destination;

    public int cooldown;

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
}
