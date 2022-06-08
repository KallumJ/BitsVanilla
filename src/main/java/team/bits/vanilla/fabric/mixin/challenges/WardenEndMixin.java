package team.bits.vanilla.fabric.mixin.challenges;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.server.network.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(EndPortalBlock.class)
public class WardenEndMixin {

    private static final int RANGE = 20;

    @Inject(
            method = "onEntityCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"
            )
    )
    public void onEntityTeleportToEnd(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof WardenEntity) {
            Box box = new Box(
                    pos.getX() - RANGE, pos.getY() - RANGE, pos.getZ() - RANGE,
                    pos.getX() + RANGE, pos.getY() + RANGE, pos.getZ() + RANGE
            );
            Collection<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                    ServerPlayerEntity.class, box, player -> true
            );

            for (ServerPlayerEntity player : nearbyPlayers) {
                ((ExtendedPlayerEntity) player).markChallengeCompleted(Challenges.WARDEN_END);
            }
        }
    }
}
