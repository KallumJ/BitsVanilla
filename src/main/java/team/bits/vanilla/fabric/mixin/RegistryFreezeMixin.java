package team.bits.vanilla.fabric.mixin;

import net.minecraft.util.registry.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(SimpleRegistry.class)
public class RegistryFreezeMixin {

    /**
     * Mojang decided to "freeze" the statistics registry. We just want to
     * bypass that so we can add custom statistics.
     */

    @Inject(
            method = "assertNotFrozen",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onAssertNotFrozen(CallbackInfo ci) {
        ci.cancel();
    }
}
