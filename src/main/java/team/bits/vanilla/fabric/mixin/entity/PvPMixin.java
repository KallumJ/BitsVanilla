package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.damage.*;
import net.minecraft.entity.player.*;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.listeners.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

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
