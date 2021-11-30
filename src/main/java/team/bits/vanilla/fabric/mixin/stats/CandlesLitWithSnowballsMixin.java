package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(AbstractCandleBlock.class)
public class CandlesLitWithSnowballsMixin {

    @Inject(
            method = "onProjectileHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/AbstractCandleBlock;setLit(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Z)V"
            )
    )
    public void onCandleLitByProjectile(World world, BlockState state, BlockHitResult hit,
                                        ProjectileEntity projectile, CallbackInfo ci) {

        if (projectile instanceof SnowballEntity) {
            Entity owner = projectile.getOwner();
            if (owner instanceof ServerPlayerEntity player) {
                StatUtils.incrementStat(player, CustomStats.CANDLES_LIT_WITH_SNOWBALLS, 1);
            }
        }
    }
}
