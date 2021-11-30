package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(AbstractSignBlock.class)
public class GlowInkRemovedMixin {

    @Inject(
            method = "onUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/SignBlockEntity;setGlowingText(Z)Z",
                    ordinal = 1 // target the second occurrence
            )
    )
    public void onGlowInkRemoved(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                                 BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        StatUtils.incrementStat((ServerPlayerEntity) player, CustomStats.GLOW_INK_REMOVED, 1);
    }
}
