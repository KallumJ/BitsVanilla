package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.block.BigDripleafBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(BigDripleafBlock.class)
public class DripleafsTiltedMixin {

    @Inject(
            method = "onEntityCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BigDripleafBlock;changeTilt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/enums/Tilt;Lnet/minecraft/sound/SoundEvent;)V"
            )
    )
    public void onTriggerTilt(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity player) {
            StatUtils.incrementStat(player, CustomStats.DRIPLEAFS_TILTED, 1);
        }
    }
}
