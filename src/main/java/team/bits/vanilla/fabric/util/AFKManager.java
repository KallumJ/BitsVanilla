package team.bits.vanilla.fabric.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.vanilla.fabric.database.player.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class AFKManager {
    public static final Map<ServerPlayerEntity, AFKCounter> PLAYER_TRACKER = new HashMap<>();
    public static final int AFK_THRESHOLD = 300; // Time in seconds
    private static final String NOW_AFK_MSG = "*%s is now AFK";
    private static final String NO_LONGER_AFK_MSG = "*%s is no longer AFK";

    public static void playerMoved(ServerPlayerEntity player) {
        AFKCounter playersAfkCounter = PLAYER_TRACKER.get(player);

        // If the player is AFK when their time gets reset, announce that they are no longer AFK
        if (playersAfkCounter.isAfk() || playersAfkCounter.isVisuallyAfk()) {
            ServerInstance.broadcast(
                    Component.text(String.format(NO_LONGER_AFK_MSG, PlayerUtils.getEffectiveName(player)))
                            .color(NamedTextColor.GRAY)
            );
        }

        playersAfkCounter.resetTimeAfk();
    }


    public static void playerConnect(ServerPlayerEntity player) {
        // When a player joins, start tracking them
        AFKCounter afkCounter = new AFKCounter();
        PLAYER_TRACKER.put(player, afkCounter);
        Scheduler.scheduleAtFixedRate(afkCounter, 0, 20);
    }

    public static void initAfkManager() {
        // Every 0.5 seconds, check whether we need to announce that a player has gone AFK
        Scheduler.scheduleAtFixedRate(() -> PLAYER_TRACKER.forEach((serverPlayerEntity, afkCounter) -> {
            if ((afkCounter.isAfk() || afkCounter.isVisuallyAfk()) && !afkCounter.isAnnounced()) {
                afkCounter.setAnnounced(true);
                ServerInstance.broadcast(
                        Component.text(String.format(NOW_AFK_MSG, PlayerUtils.getEffectiveName(serverPlayerEntity)))
                                .color(NamedTextColor.GRAY));
            }
        }), 0, 10);
    }

    public static void playerDisconnect(ServerPlayerEntity player) {
        // When a player leaves, stop tracking them
        PLAYER_TRACKER.remove(player);
    }

    public static boolean isAFK(ServerPlayerEntity player) {
        return PLAYER_TRACKER.get(player).isAfk();
    }

    public static void makeVisuallyAfk(ServerPlayerEntity player) {
        AFKCounter afkCounter = AFKManager.PLAYER_TRACKER.get(player);
        afkCounter.setVisuallyAfk();
    }
}

