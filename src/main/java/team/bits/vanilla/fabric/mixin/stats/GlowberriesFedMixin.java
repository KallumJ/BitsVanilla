package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(FoxEntity.class)
public class GlowberriesFedMixin {

    @Inject(
            method = "eat",
            at = @At("HEAD")
    )
    public void onFoxEat(PlayerEntity player, Hand hand, ItemStack stack, CallbackInfo ci) {
        if (stack.getItem().equals(Items.GLOW_BERRIES)) {
            StatUtils.incrementStat((ServerPlayerEntity) player, CustomStats.GLOWBERRIES_FED, 1);
        }
    }
}
