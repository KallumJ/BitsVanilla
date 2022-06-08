package team.bits.vanilla.fabric.teleport;

import net.minecraft.entity.player.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.teleport.*;

import java.util.*;

public class Teleport {

    private final PlayerEntity player;
    private final Location destination;

    private int warmup;

    public Teleport(@NotNull PlayerEntity player, @NotNull Location destination, int warmup) {
        this.player = Objects.requireNonNull(player);
        this.destination = Objects.requireNonNull(destination);
        this.warmup = warmup;
    }

    public @NotNull PlayerEntity getPlayer() {
        return this.player;
    }

    public @NotNull Location getDestination() {
        return this.destination;
    }

    public int getRemainingWarmup() {
        return this.warmup;
    }

    public void tick(int elapsedTime) {
        this.warmup -= elapsedTime;
    }
}
