package team.bits.vanilla.fabric.listeners;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.misc.PlayerConnectEvent;
import team.bits.vanilla.fabric.util.AFKManager;

public class PlayerConnectListener implements PlayerConnectEvent {

    @Override
    public void onPlayerConnect(@NotNull ServerPlayerEntity player, @NotNull ClientConnection connection) {
        AFKManager.playerConnect(player);
    }
}
