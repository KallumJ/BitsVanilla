package team.bits.vanilla.fabric.event.misc;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PlayerDisconnectEvent {

    Event<PlayerDisconnectEvent> EVENT = EventFactory.createArrayBacked(PlayerDisconnectEvent.class,
            (listeners) -> (player, connection) -> {
                for (PlayerDisconnectEvent listener : listeners) {
                    listener.onPlayerDisconnect(player, connection);
                }
            }
    );

    void onPlayerDisconnect(@NotNull ServerPlayerEntity player, @NotNull ClientConnection connection);
}
