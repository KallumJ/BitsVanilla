package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.mob.*;
import net.minecraft.world.explosion.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(CreeperEntity.class)
public class CreeperMixin {

    @ModifyVariable(
            method = "explode",
            at = @At("STORE"),
            ordinal = 0
    )
    private Explosion.DestructionType preventDamage(Explosion.DestructionType explosionType) {
        return Explosion.DestructionType.NONE;
    }
}
