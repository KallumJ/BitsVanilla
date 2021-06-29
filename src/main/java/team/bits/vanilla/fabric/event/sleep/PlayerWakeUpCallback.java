package team.bits.vanilla.fabric.event.sleep;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PlayerWakeUpCallback {

    Event<PlayerWakeUpCallback> EVENT = EventFactory.createArrayBacked(PlayerWakeUpCallback.class,
            (listeners) -> (player) -> {
                for (PlayerWakeUpCallback listener : listeners) {
                    listener.onWakeUp(player);
                }
            });

    void onWakeUp(@NotNull PlayerEntity player);
}
