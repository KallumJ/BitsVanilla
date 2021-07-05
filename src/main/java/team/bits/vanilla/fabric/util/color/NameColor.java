package team.bits.vanilla.fabric.util.color;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public record NameColor(String name, Color color) {

    public NameColor(@NotNull String name, @NotNull Color color) {
        this.name = Objects.requireNonNull(name);
        this.color = Objects.requireNonNull(color);
    }

    public int getRGB() {
        return this.color.getRGB();
    }
}
