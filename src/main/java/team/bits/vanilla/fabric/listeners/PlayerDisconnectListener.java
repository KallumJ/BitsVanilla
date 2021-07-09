package team.bits.vanilla.fabric.listeners;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.vanilla.fabric.event.misc.PlayerDisconnectEvent;
import team.bits.vanilla.fabric.util.AFKManager;

public class PlayerDisconnectListener implements PlayerDisconnectEvent {
    @Override
    public void onPlayerDisconnect(@NotNull ServerPlayerEntity player, @NotNull ClientConnection connection) {
        AFKManager.playerDisconnect(player);
    }
}
