package team.bits.vanilla.fabric.listeners;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.misc.PlayerMoveEvent;
import team.bits.vanilla.fabric.util.AFKManager;

public class PlayerMoveListener implements PlayerMoveEvent.Listener {

    @Override
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        AFKManager.playerMoved((ServerPlayerEntity) event.getPlayer());
    }
}
