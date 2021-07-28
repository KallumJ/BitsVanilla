package team.bits.vanilla.fabric.mixin.shutdown;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    @Final
    static Logger LOGGER;

    @Shadow
    @Final
    private ServerNetworkIo networkIo;

    @Inject(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"
            )
    )
    public void duringShutdown(CallbackInfo ci) {
        LOGGER.info("Closing any remaining connections");
        this.networkIo.stop();
    }
}
