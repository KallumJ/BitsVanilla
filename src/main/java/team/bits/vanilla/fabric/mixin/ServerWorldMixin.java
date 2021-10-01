package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
