package team.bits.vanilla.fabric.event.sleep;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface PlayerSleepCallback {

    Event<PlayerSleepCallback> EVENT = EventFactory.createArrayBacked(PlayerSleepCallback.class,
            (listeners) -> (player) -> {
                for (PlayerSleepCallback listener : listeners) {
                    listener.onSleep(player);
                }
            });

    void onSleep(@NotNull PlayerEntity player);
}
