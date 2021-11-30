package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(SpawnRestriction.class)
public class SpawnRestrictionMixin {

    @Inject(
            method = "canSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T extends Entity> void canSpawn(EntityType<T> type, ServerWorldAccess world,
                                                    SpawnReason spawnReason, BlockPos pos, Random random,
                                                    CallbackInfoReturnable<Boolean> cir) {

//        // check if this is a natural mob spawn
//        if (spawnReason == SpawnReason.NATURAL) {
//
//            // check if mobs aren't allowed to spawn in the selected location
//            Location location = new Location(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), world.toServerWorld());
//            if (!SpawnPreventionHandler.INSTANCE.canSpawn(location)) {
//
//                // if so, don't allow spawning
//                cir.setReturnValue(false);
//            }
//        }
    }
}
