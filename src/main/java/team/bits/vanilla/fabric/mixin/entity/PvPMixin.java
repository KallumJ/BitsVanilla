package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.player.*;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import team.bits.vanilla.fabric.util.*;

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
        return ((ExtendedPlayerEntity) self).hasPvpEnabled() && ((ExtendedPlayerEntity) attacker).hasPvpEnabled();
    }
}
