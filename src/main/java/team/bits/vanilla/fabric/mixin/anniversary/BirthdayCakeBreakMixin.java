package team.bits.vanilla.fabric.mixin.anniversary;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.nibbles.event.base.EventManager;
import team.bits.nibbles.event.interaction.PlayerBlockBreakEvent;
import team.bits.nibbles.teleport.Location;
import team.bits.nibbles.utils.Scheduler;
import team.bits.vanilla.fabric.challenges.ChallengeRewardItems;
import team.bits.vanilla.fabric.util.BirthdayCakeUtils;


@Mixin(World.class)
public class BirthdayCakeBreakMixin {

    static {
        EventManager.INSTANCE.registerEvents((PlayerBlockBreakEvent.Listener) event -> {
            BlockPos pos = event.getPos();
            ServerWorld sWorld = event.getWorld();
            Location location = new Location(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), sWorld);
            if (BirthdayCakeUtils.isLocationBirthdayCake(location)) {
                removeCake(location);
            }
        });
    }

    private static void removeCake(Location location) {
        World world = location.world();
        BirthdayCakeUtils.removeBirthdayCake(location);
        world.spawnEntity(new ItemEntity(world, location.x(), location.y(), location.z(), ChallengeRewardItems.BIRTHDAY_CAKE));
    }

    @Inject(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            at = @At("HEAD")
    )
    public void breakCake(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        ServerWorld sWorld = (ServerWorld) world;
        BlockState currentState = world.getBlockState(pos);
        Location location = new Location(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), sWorld);

        // This is run on a delay as we must let the PlayerBlockBreak event fire first, as we need to guarantee
        // If a player breaks the cake, it drops, no matter what.
        // The following code is for dealing with cakes dropped by other means, which suffers from an issue where
        // If the cake is on the last slice, it is considered finished, and will not drop, but will not respawn
        Scheduler.schedule(() -> {
            // If that cake is a birthday cake
            if (BirthdayCakeUtils.isLocationBirthdayCake(location)) {

                // If state is cake and is being replaced with something that isnt cake
                if (BirthdayCakeUtils.isCake(currentState) && !BirthdayCakeUtils.isCake(state)) {
                    // If that cake is unfinished, i.e, hasnt been fully eaten, and needs to pop off as an item
                    if (BirthdayCakeUtils.isCakeUnfinished(currentState)) {
                        removeCake(location);
                    }

                }
            }
        }, 5);
    }
}
