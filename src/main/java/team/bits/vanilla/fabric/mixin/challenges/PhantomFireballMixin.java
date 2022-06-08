package team.bits.vanilla.fabric.mixin.challenges;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(FireballEntity.class)
public class PhantomFireballMixin {

    private static final int SEARCH_RADIUS = 5; // blocks

    @Inject(
            method = "onCollision",
            at = @At(
                    value = "RETURN",
                    target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"
            )
    )
    public void onCollision(HitResult hitResult, CallbackInfo ci) {
        FireballEntity fireball = (FireballEntity) (Object) this;

        // If the fireball owner is a player
        if (fireball.getOwner() instanceof ServerPlayerEntity player) {

            List<PhantomEntity> killedPhantoms = player.getWorld().getEntitiesByType( // Get entities
                    TypeFilter.instanceOf(PhantomEntity.class), // That are phantoms
                    getPhantomSearchBox(hitResult), // Within the bounds of the explosion
                    LivingEntity::isDead // And are now dead
            );

            // If any phantoms were killed by the explosion
            if (!killedPhantoms.isEmpty()) {
                // Grant challenge
                ((ExtendedPlayerEntity) player).markChallengeCompleted(Challenges.PHANTOM_FIREBALL);
            }
        }
    }

    public Box getPhantomSearchBox(HitResult hitResult) {
        Vec3d hitPos = hitResult.getPos();
        Vec3d searchVec = new Vec3d(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
        return new Box(hitPos.subtract(searchVec), hitPos.add(searchVec));
    }
}
