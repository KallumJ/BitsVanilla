package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShearsItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(ShearsItem.class)
public class KelpTrimmedMixin {

    @Inject(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"
            )
    )
    public void onKelpTrimmed(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        if (player != null) {

            World world = context.getWorld();
            BlockPos blockPos = context.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (block.equals(Blocks.KELP_PLANT) || block.equals(Blocks.KELP)) {
                StatUtils.incrementStat(player, CustomStats.KELP_TRIMMED, 1);
            }
        }
    }
}
