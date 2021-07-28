package team.bits.vanilla.fabric.mixin.shutdown;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public class DedicatedServerMixin {

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(
            method = "exit",
            at = @At("RETURN")
    )
    public void onFinalExit(CallbackInfo ci) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted during final shutdown delay, opting for instant suicide");
            }

            LOGGER.info("Shutdown complete, time to die!");
            System.exit(0);

        }, "Final shutdown thread").start();
    }
}
