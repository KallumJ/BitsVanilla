package team.bits.vanilla.fabric.event.sleep;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface PlayerMoveCallback {

    Event<PlayerMoveCallback> EVENT = EventFactory.createArrayBacked(PlayerMoveCallback.class,
            // there's some special stuff here cuz this event is called very frequently (many times per second)
            // so we use a special overload of the method to improve performance in our use case
            (player, moveVector) -> {
            },
            (listeners) -> (player, moveVector) -> {
                for (PlayerMoveCallback listener : listeners) {
                    listener.onPlayerMove(player, moveVector);
                }
            }
    );

    void onPlayerMove(@NotNull PlayerEntity player, @NotNull Vec3d moveVector);
}
