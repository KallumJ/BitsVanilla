package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

import java.util.Objects;

@Mixin(HoneycombItem.class)
public class CopperWaxedMixin {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"
            ),
            method = "useOnBlock"
    )
    public void onCopperWaxed(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        StatUtils.incrementStat((ServerPlayerEntity) Objects.requireNonNull(context.getPlayer()), CustomStats.COPPER_WAXED, 1);
    }
}
