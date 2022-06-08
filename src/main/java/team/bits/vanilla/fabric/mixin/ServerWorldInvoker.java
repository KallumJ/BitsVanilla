package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(ServerWorld.class)
public interface ServerWorldInvoker {
    @Invoker("resetWeather")
    void invokeResetWeather();
}

