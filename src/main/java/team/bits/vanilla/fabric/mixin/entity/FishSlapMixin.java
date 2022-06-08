package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ServerPlayerEntity.class)
public class FishSlapMixin {

    @Inject(
            method = "damage",
            at = @At("HEAD")
    )
    public void onPlayerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        final ServerPlayerEntity attackedPlayer = ServerPlayerEntity.class.cast(this);

        // check if the damage source is an entity
        if (source instanceof EntityDamageSource) {

            // check if the entity is another player
            final Entity entity = source.getSource();
            if (entity instanceof ServerPlayerEntity attackingPlayer) {

                // check if the attacking player is holding a raw cod
                final PlayerInventory inventory = attackingPlayer.getInventory();
                final ItemStack itemInHand = inventory.getMainHandStack();
                if (itemInHand.getRegistryEntry().isIn(ItemTags.FISHES)) {
                    // remove 1 fish from their hand
                    itemInHand.decrement(1);

                    // launch the attacked player
                    Vec3d launchVector = getLaunchVector(attackingPlayer);
                    attackedPlayer.setVelocity(attackedPlayer.getVelocity().add(launchVector));
                    attackedPlayer.velocityModified = true;
                }
            }
        }
    }

    /**
     * Get a launch vector based on the direction in which a player is looking
     */
    private static @NotNull Vec3d getLaunchVector(@NotNull Entity entity) {
        final float HORIZONTAL_POWER = 0.5f;
        final float VERTICAL_POWER = 0.2f;

        // get the player's yaw
        float yaw = entity.getYaw();

        // convert the yaw and pitch to an X and Z component of a vector
        // we don't care about the Y component since we will be replacing
        // that with an upward force
        double x = -Math.sin(Math.toRadians(yaw));
        double z = Math.cos(Math.toRadians(yaw));

        // create a vector with the X and Z component and scale the power
        Vec3d horizontalLookVector = new Vec3d(x, 0, z)
                .normalize()
                .multiply(HORIZONTAL_POWER);

        // return a vector with the scaled X and Z components and an upward Y force
        return new Vec3d(horizontalLookVector.x, VERTICAL_POWER, horizontalLookVector.z);
    }
}
