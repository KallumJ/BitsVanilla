package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.listeners.DuelHandler;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public class PvPMixin {

    @Redirect(
            method = "shouldDamagePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;shouldDamagePlayer(Lnet/minecraft/entity/player/PlayerEntity;)Z"
            )
    )
    public boolean customShouldDamagePlayer(PlayerEntity self, PlayerEntity attacker) {
        return ((ExtendedPlayerEntity) self).getDuelTarget()
                .filter(target -> target.equals(attacker))
                .isPresent();
    }

    @Inject(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"
            )
    )
    public void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) this;
        Optional<PlayerEntity> target = ePlayer.getDuelTarget();
        target.ifPresent(winner ->
                DuelHandler.finishDuel(winner, PlayerEntity.class.cast(this))
        );
    }
}
