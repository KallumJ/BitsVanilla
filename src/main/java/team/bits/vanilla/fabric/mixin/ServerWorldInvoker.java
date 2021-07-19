package team.bits.vanilla.fabric.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerWorld.class)
public interface ServerWorldInvoker {
    @Invoker("resetWeather")
    void invokeResetWeather();
}

