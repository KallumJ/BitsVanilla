package team.bits.vanilla.fabric.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Utils {

    private Utils() {
    }

    /**
     * Formats remaining time in an easily readable format.
     * Time longer than 60 seconds will be formatted as '%s minutes'.
     * Time shorter than 60 seconds will be formatted as '%s seconds'.
     *
     * @param start time at which the timer started in milliseconds
     * @param now   current time in milliseconds
     * @param total total time in milliseconds
     */
    public static @NotNull String formatTimeRemaining(long start, long now, long total) {
        final double MILLIS_TO_MINUTES = 1.66667e-5;
        final double MINUTES_TO_SECONDS = 60.0;

        double remaining = ((start + total) - now) * MILLIS_TO_MINUTES;
        if (remaining > 1) {
            int minutes = (int) Math.ceil(remaining);
            return String.format("%s minutes", minutes);
        } else {
            int seconds = (int) Math.ceil(remaining * MINUTES_TO_SECONDS);
            return String.format("%s second%s", seconds, seconds != 1 ? "s" : "");
        }
    }

    public static void teleport(@NotNull ServerPlayerEntity player, @NotNull Location location) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(location);

        final ServerWorld world = location.world();
        final Vec3d position = location.position();

        // load the chunk at the destination
        ChunkPos chunkPos = new ChunkPos(new BlockPos(position));
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());

        // make sure the player isn't riding an entity or sleeping
        player.stopRiding();
        if (player.isSleeping()) {
            player.wakeUp(true, true);
        }

        // teleport the player to the destination
        if (world == player.world) {
            player.networkHandler.requestTeleport(position.x, position.y, position.z, player.getYaw(), player.getPitch());
        } else {
            player.teleport(world, position.x, position.y, position.z, player.getYaw(), player.getPitch());
        }
    }
}
