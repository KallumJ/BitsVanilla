package team.bits.vanilla.fabric.listeners;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.misc.PlayerDisconnectEvent;
import team.bits.vanilla.fabric.util.AFKManager;

public class PlayerDisconnectListener implements PlayerDisconnectEvent.Listener {

    @Override
    public void onPlayerDisonnect(@NotNull PlayerDisconnectEvent event) {
        AFKManager.playerDisconnect(event.getPlayer());
    }
}
