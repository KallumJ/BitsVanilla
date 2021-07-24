package team.bits.vanilla.fabric.util;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public final class Utils {

    private Utils() {
    }

    public static void updatePlayerDisplayName(@NotNull ServerPlayerEntity player) {
        ServerInstance.get().getPlayerManager().sendToAll(
                new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player)
        );
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

    public static @NotNull String fancyFormat(@NotNull String input) {
        Objects.requireNonNull(input);
        String[] words = input.replace("_", " ").split(" ");
        StringBuilder builder = new StringBuilder();
        Arrays.stream(words).forEach((word) -> {
            builder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(' ');
        });
        return builder.toString().trim();
    }
}
