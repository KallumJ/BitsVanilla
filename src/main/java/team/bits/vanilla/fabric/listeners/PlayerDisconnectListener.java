package team.bits.vanilla.fabric.listeners;

import org.jetbrains.annotations.*;
import team.bits.nibbles.event.server.*;
import team.bits.vanilla.fabric.util.*;

public class PlayerDisconnectListener implements PlayerDisconnectEvent.Listener {

    @Override
    public void onPlayerDisonnect(@NotNull PlayerDisconnectEvent event) {
        AFKManager.playerDisconnect(event.getPlayer());
    }
}
