package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(
            at = @At(value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;"),
            method = "handleSleeping",
            cancellable = true
    )
    public void onSleepHandle(CallbackInfo ci) {
        // Just before Minecraft iterates through all the players and sends a message to their action bar,
        // return out of this method
        ci.cancel();
    }

}
