package team.bits.vanilla.fabric.mixin.anniversary;

import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.nibbles.teleport.Location;
import team.bits.vanilla.fabric.util.BirthdayCakeUtils;


@Mixin(CakeBlock.class)
public class BirthdayCakeEatMixin {

    @Inject(
            method = "tryEat",
            at = @At("TAIL")
    )
    private static void finishEating(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
        ServerWorld sWorld = (ServerWorld) player.getWorld();
        Location location = new Location(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), sWorld);

        if (BirthdayCakeUtils.isLocationBirthdayCake(location) && !BirthdayCakeUtils.isCakeUnfinished(state)) {
            sWorld.setBlockState(pos, BirthdayCakeUtils.BIRTHDAY_CAKE_STATE);
        }
    }

}