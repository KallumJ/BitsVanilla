package team.bits.vanilla.fabric.listeners;

import org.jetbrains.annotations.*;
import team.bits.nibbles.event.server.*;
import team.bits.vanilla.fabric.util.*;


public class PlayerConnectListener implements PlayerConnectEvent.Listener {

    @Override
    public void onPlayerConnect(@NotNull PlayerConnectEvent event) {
        AFKManager.playerConnect(event.getPlayer());
    }
}
