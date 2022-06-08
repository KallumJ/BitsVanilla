package team.bits.vanilla.fabric.database;

import org.jetbrains.annotations.*;
import team.bits.nibbles.teleport.*;

import java.util.*;

public record Warp(String name, Location location) {

    public Warp(@NotNull String name, @NotNull Location location) {
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
    }
}
