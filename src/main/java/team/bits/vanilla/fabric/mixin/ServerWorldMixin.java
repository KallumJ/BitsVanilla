package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(
            method = "sendSleepingStatus",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onSleepHandle(CallbackInfo ci) {
        ci.cancel();
    }
}
