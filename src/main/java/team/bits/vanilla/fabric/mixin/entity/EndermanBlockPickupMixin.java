package team.bits.vanilla.fabric.mixin.entity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal")
public class EndermanBlockPickupMixin {

    @Inject(
            at = @At("HEAD"),
            method = {"canStart"},
            cancellable = true
    )
    public void preventBlockPickup(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
