package team.bits.vanilla.fabric.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.utils.Scheduler;
import team.bits.nibbles.utils.ServerInstance;
import team.bits.vanilla.fabric.database.player.PlayerUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {

    public static final Map<UUID, AFKCounter> PLAYER_TRACKER = new HashMap<>();
    public static final int AFK_THRESHOLD = 300; // Time in seconds

    private static final String NOW_AFK_MSG = "* %s is now AFK";
    private static final String NO_LONGER_AFK_MSG = "* %s is no longer AFK";

    private AFKManager() {
    }

    public static void playerMoved(ServerPlayerEntity player) {
        AFKCounter playersAfkCounter = PLAYER_TRACKER.get(player.getUuid());

        // if the afk counter is null it means the player hasn't fully
        // logged in yet, so we don't want to do anything.
        if (playersAfkCounter == null) {
            return;
        }

        boolean wasAfk = false;

        // If the player is AFK when their time gets reset, announce that they are no longer AFK
        if (playersAfkCounter.isAfk() || playersAfkCounter.isVisuallyAfk()) {
            ServerInstance.broadcast(
                    Component.text(String.format(NO_LONGER_AFK_MSG, PlayerUtils.getEffectiveName(player)))
                            .color(NamedTextColor.GRAY)
            );
            ((ExtendedPlayerEntity) player).setAFK(false);
            wasAfk = true;
        }

        playersAfkCounter.resetTimeAfk();

        // we want to update the display name after we reset the time
        // but only if they were afk before to reduce packet traffic
        if (wasAfk) {
            Utils.updatePlayerDisplayName(player);
        }
    }


    public static void playerConnect(ServerPlayerEntity player) {
        // When a player joins, start tracking them
        AFKCounter afkCounter = new AFKCounter();
        PLAYER_TRACKER.put(player.getUuid(), afkCounter);
        Scheduler.scheduleAtFixedRate(afkCounter, 0, 20);
    }

    public static void initAfkManager() {
        // Every 0.5 seconds, check whether we need to announce that a player has gone AFK
        Scheduler.scheduleAtFixedRate(() -> PLAYER_TRACKER.forEach((playerUUID, afkCounter) -> {
            ServerPlayerEntity player = ServerInstance.get().getPlayerManager().getPlayer(playerUUID);
            if (player != null) {
                if ((afkCounter.isAfk() || afkCounter.isVisuallyAfk()) && !afkCounter.isAnnounced()) {
                    afkCounter.setAnnounced(true);
                    ServerInstance.broadcast(
                            Component.text(String.format(NOW_AFK_MSG, PlayerUtils.getEffectiveName(player)))
                                    .color(NamedTextColor.GRAY));
                    Utils.updatePlayerDisplayName(player);

                    ((ExtendedPlayerEntity) player).setAFK(true);
                }
            }
        }), 0, 10);
    }

    public static void playerDisconnect(ServerPlayerEntity player) {
        // When a player leaves, stop tracking them
        PLAYER_TRACKER.remove(player.getUuid());
    }

    public static boolean isAFK(ServerPlayerEntity player) {
        return PLAYER_TRACKER.containsKey(player.getUuid()) && PLAYER_TRACKER.get(player.getUuid()).isAfk();
    }

    public static boolean isVisuallyAfk(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        if (PLAYER_TRACKER.containsKey(playerUUID)) {
            AFKCounter counter = PLAYER_TRACKER.get(playerUUID);
            return counter.isVisuallyAfk() || counter.isAfk();
        }
        return false;
    }

    public static void makeVisuallyAfk(ServerPlayerEntity player) {
        AFKCounter afkCounter = AFKManager.PLAYER_TRACKER.get(player.getUuid());
        afkCounter.setVisuallyAfk();
    }
}

