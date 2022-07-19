package team.bits.vanilla.fabric.mixin.anniversary;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.nibbles.teleport.Location;
import team.bits.vanilla.fabric.util.BirthdayCakeUtils;

import java.util.LinkedList;
import java.util.List;

@Mixin(Block.class)
public class BirthdayCakeCandleMixin {

    @Inject(
            method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)Ljava/util/List;",
            at = @At("TAIL"),
            cancellable = true
    )
    private static void preventCandle(BlockState state, ServerWorld world, BlockPos pos, BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> cir) {
        // If location is birthday cake
        Location location = new Location(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), world);
        if (BirthdayCakeUtils.isLocationBirthdayCake(location) && BirthdayCakeUtils.isCake(state)) {
            List<ItemStack> itemStacks = cir.getReturnValue();

            // Create new list of item stacks, with no candles in it
            List<ItemStack> newList = new LinkedList<>();
            for (ItemStack itemStack : itemStacks) {
               if (!itemStack.isIn(ItemTags.CANDLES)) {
                   newList.add(itemStack);
               }
            }

            // Return that instead
            cir.setReturnValue(newList);
        }
    }
}
