package team.bits.vanilla.fabric.util;

import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.*;

public record NameColor(String name, Color color) {

    public NameColor(@NotNull String name, @NotNull Color color) {
        this.name = Objects.requireNonNull(name);
        this.color = Objects.requireNonNull(color);
    }

    public int getRGB() {
        return this.color.getRGB();
    }
}
