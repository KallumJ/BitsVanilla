package team.bits.vanilla.fabric.database.warp;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.teleport.Location;

import java.util.Objects;

public record Warp(String name, Location location) {

    public Warp(@NotNull String name, @NotNull Location location) {
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
    }
}
