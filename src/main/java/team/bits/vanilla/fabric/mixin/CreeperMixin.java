package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CreeperEntity.class)
public class CreeperMixin {

    @ModifyVariable(method = "explode", at = @At("STORE"), ordinal = 0)
    private Explosion.DestructionType preventDamge(Explosion.DestructionType explosionType) {
        return Explosion.DestructionType.NONE;
    }
}
