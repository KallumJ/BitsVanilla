package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.util.Scheduler;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class SchedulerHandler {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"
            ),
            method = "tick"
    )
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        Scheduler.tick();
    }
}
