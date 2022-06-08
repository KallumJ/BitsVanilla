package team.bits.vanilla.fabric.mixin.pregen;

import net.minecraft.server.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerMixin {

    @Invoker("tick")
    @SuppressWarnings({"UnusedReturnValue", "UnnecessaryInterfaceModifier"})
    public boolean c_tick();
}
