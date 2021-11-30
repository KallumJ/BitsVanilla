package team.bits.vanilla.fabric.listeners;

import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.misc.PlayerConnectEvent;
import team.bits.vanilla.fabric.util.AFKManager;

public class PlayerConnectListener implements PlayerConnectEvent.Listener {

    @Override
    public void onPlayerConnect(@NotNull PlayerConnectEvent event) {
        AFKManager.playerConnect(event.getPlayer());
    }
}
