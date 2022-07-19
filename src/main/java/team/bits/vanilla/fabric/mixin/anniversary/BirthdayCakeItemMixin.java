package team.bits.vanilla.fabric.mixin.anniversary;

import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.nibbles.teleport.Location;
import team.bits.vanilla.fabric.challenges.ChallengeRewardItems;
import team.bits.vanilla.fabric.util.BirthdayCakeUtils;

@Mixin(BlockItem.class)
public class BirthdayCakeItemMixin {

    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At("TAIL")
    )
    public void place(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (isBirthdayCake(context.getStack()) && cir.getReturnValue().isAccepted()) {
            BlockPos blockPos = context.getBlockPos();
            ServerWorld world = (ServerWorld) context.getWorld();
            Location location = new Location(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), world);

            world.setBlockState(blockPos, BirthdayCakeUtils.BIRTHDAY_CAKE_STATE);

            BirthdayCakeUtils.addBirthdayCake(location);
        }
    }

    private boolean isBirthdayCake(ItemStack stack) {
        if (stack.getNbt() != null) {
            return stack.getNbt().getInt(ChallengeRewardItems.BIRTHDAY_CAKE_NBT) == 1;
        }
        return false;
    }
}
