package team.bits.vanilla.fabric.event.damage;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface PlayerDamageCallback {

    Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(PlayerDamageCallback.class,
            (listeners) -> (player, source, moveVector) -> {
                for (PlayerDamageCallback listener : listeners) {
                    listener.onPlayerDamage(player, source, moveVector);
                }
            }
    );

    void onPlayerDamage(@NotNull PlayerEntity player, @NotNull DamageSource source, float amount);
}
