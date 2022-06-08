package team.bits.vanilla.fabric.mixin.shutdown;

import net.minecraft.server.dedicated.*;
import org.apache.logging.log4j.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

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
