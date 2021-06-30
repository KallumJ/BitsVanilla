package team.bits.vanilla.fabric.teleport;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bits.vanilla.fabric.util.Location;

import java.util.Objects;

public class Teleport {

    private final PlayerEntity player;
    private final Location destination;
    private final Runnable cancelCallback;

    public int cooldown;

    public Teleport(@NotNull PlayerEntity player, @NotNull Location destination, @Nullable Runnable cancelCallback, int cooldown) {
        this.player = Objects.requireNonNull(player);
        this.destination = Objects.requireNonNull(destination);
        this.cancelCallback = cancelCallback;
        this.cooldown = cooldown;
    }

    public @NotNull PlayerEntity getPlayer() {
        return this.player;
    }

    public @NotNull Location getDestination() {
        return this.destination;
    }

    public void runCancel() {
        if (this.cancelCallback != null) {
            this.cancelCallback.run();
        }
    }
}
