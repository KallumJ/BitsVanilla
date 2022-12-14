package team.bits.vanilla.fabric.util;

import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;

import java.util.*;

public class AFKManager {

    public static final Map<UUID, AFKCounter> PLAYER_TRACKER = new HashMap<>();
    public static final int AFK_THRESHOLD = 300; // Time in seconds

    private static final Map<UUID, Float> prevYawMap = new HashMap<>();

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

            Text text = Text.literal(String.format(NO_LONGER_AFK_MSG, PlayerApiUtils.getEffectiveName(player)))
                    .styled(style -> style.withColor(Formatting.GRAY));
            ServerInstance.broadcast(text);
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
                    Text text = Text.literal(String.format(NOW_AFK_MSG, PlayerApiUtils.getEffectiveName(player)))
                            .styled(style -> style.withColor(Formatting.GRAY));
                    ServerInstance.broadcast(text);
                    Utils.updatePlayerDisplayName(player);

                    ((ExtendedPlayerEntity) player).setAFK(true);
                }

                // If the player moves their mouse, tell the AFKManager the player has moved
                float currentYaw = player.getHeadYaw();
                float prevYaw = prevYawMap.getOrDefault(playerUUID, currentYaw);

                if (prevYaw != currentYaw) {
                    playerMoved(player);
                }
                prevYawMap.put(playerUUID, currentYaw);
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

