package team.bits.vanilla.fabric.event.misc;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PlayerConnectEvent {

    Event<PlayerConnectEvent> EVENT = EventFactory.createArrayBacked(PlayerConnectEvent.class,
            (listeners) -> (player, connection) -> {
                for (PlayerConnectEvent listener : listeners) {
                    listener.onPlayerConnect(player, connection);
                }
            }
    );

    void onPlayerConnect(@NotNull ServerPlayerEntity player, @NotNull ClientConnection connection);
}
