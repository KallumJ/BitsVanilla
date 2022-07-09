package team.bits.vanilla.fabric.mixin;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorMixin {

    @Inject(
            method = "isNether",
            at = @At("TAIL"),
            cancellable = true
    )
    private static void overrideDimension(World world, CallbackInfoReturnable<Boolean> cir) {
        String dimensionStr = world.getRegistryKey().getValue().toString();
        if (!dimensionStr.contains("overworld")) {
            cir.setReturnValue(true);
        }
    }
}
