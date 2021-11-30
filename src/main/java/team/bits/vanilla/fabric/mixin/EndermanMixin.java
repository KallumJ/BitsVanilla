package team.bits.vanilla.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal")
public class EndermanMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            at = @At("HEAD"),
            method = {"canStart", "method_6264"},
            cancellable = true
    )
    public void preventBlockPickup(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
