package team.bits.vanilla.fabric.mixin.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(AllayEntity.class)
public abstract class AllayBreedingMixin implements ExtendedAllayEntity {
    private static final Item BREEDING_ITEM = Items.GOLDEN_APPLE;
    private static final long TIME_BETWEEN_BREEDS = 5 * 60 * 20; // mins - ticks
    private static final int ALLAY_SEARCH_RANGE = 20; // blocks around allay
    private static final int FEEDER_SEARCH_RANGE = 10; // blocks around ally

    private long timeLastBred;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void breedAllay(CallbackInfo ci) {
        AllayEntity allay = AllayEntity.class.cast(this);
        World world = allay.getWorld();
        long currentTime = world.getTime();

        // If allay is holding the breeding item
        ItemStack itemInAllayHand = allay.getMainHandStack();
        if (itemInAllayHand.isOf(BREEDING_ITEM)) {

            // If allay is ready to breed
            if (currentTime - timeLastBred > TIME_BETWEEN_BREEDS) {

                // Find nearby allays
                List<AllayEntity> nearbyAllays = getNearbyPotentialPartners(allay);

                // If there are any partners
                if (!nearbyAllays.isEmpty()) {
                    AllayEntity nearbyAllay = nearbyAllays.get(0);

                    // Move the partners towards each other
                    allay.getNavigation().startMovingTo(nearbyAllay, 1.0);

                    // If they are within 1 block, breed
                    BlockPos allayPos = allay.getBlockPos();
                    if (allayPos.isWithinDistance(nearbyAllay.getBlockPos(), 1)) {
                        breedAllays(allay, nearbyAllay);
                    }
                }
            }


        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void attractToFood(CallbackInfo ci) {
        AllayEntity allay = AllayEntity.class.cast(this);
        ExtendedAllayEntity eAllay = (ExtendedAllayEntity) allay;

        if (eAllay.isReadyToBreed() && !allay.getMainHandStack().isOf(BREEDING_ITEM)) {
            List<ServerPlayerEntity> nearbyFeeders = getNearbyFeeders(allay);

            if (!nearbyFeeders.isEmpty()) {
                allay.getNavigation().startMovingTo(nearbyFeeders.get(0), 1.0);
            }
        }
    }

    private List<ServerPlayerEntity> getNearbyFeeders(AllayEntity allay) {
        Box searchBox = generateSearchBox(allay, FEEDER_SEARCH_RANGE);

        World world = allay.getWorld();

        // Return nearby players holding the allays breeding item
        return world.getEntitiesByType(TypeFilter.instanceOf(ServerPlayerEntity.class),
                searchBox,
                player -> player.getMainHandStack().isOf(BREEDING_ITEM)
        );
    }

    private List<AllayEntity> getNearbyPotentialPartners(AllayEntity sourceAllay) {
        // Create search box
        Box searchBox = generateSearchBox(sourceAllay, ALLAY_SEARCH_RANGE);

        World world = sourceAllay.getWorld();

        // Return allays, within the search box, and that are breedable ;)
        return world.getEntitiesByType(
                TypeFilter.instanceOf(AllayEntity.class),
                searchBox,
                allayEntity -> {
                    ExtendedAllayEntity eAllay = (ExtendedAllayEntity) allayEntity;

                    return allayEntity.getMainHandStack().isOf(BREEDING_ITEM) && // Allay is holding breeding item
                            !allayEntity.equals(sourceAllay) && // Allay isnt the same as the allay looking for a partner
                            eAllay.isReadyToBreed(); // They are ready to breed
                }
        );
    }

    private Box generateSearchBox(Entity centerEntity, int range) {
        BlockPos pos = centerEntity.getBlockPos();
        BlockPos corner1 = pos.add(range, range, range);
        BlockPos corner2 = pos.add(-range, -range, -range);
        return new Box(corner1, corner2);
    }

    private void breedAllays(AllayEntity mother, AllayEntity father) {
        World world = mother.getWorld();
        BlockPos motherPos = mother.getBlockPos();

        // Spawn breeding particles
        ParticleUtils.spawnParticle(
                world, ParticleTypes.HEART, Vec3d.ofCenter(motherPos),
                5, 0.5, 0.5, 0.5
        );

        // Spawn baby
        AllayEntity baby = new AllayEntity(EntityType.ALLAY, world);
        baby.setPosition(Vec3d.ofCenter(motherPos));
        world.spawnEntity(baby);

        // Consume breeding items
        dropItemInHand(mother);
        dropItemInHand(father);

        // Set time since last bred
        long time = world.getTime();
        ExtendedAllayEntity eMother = (ExtendedAllayEntity) mother;
        ExtendedAllayEntity eFather = (ExtendedAllayEntity) father;
        ExtendedAllayEntity eBaby = (ExtendedAllayEntity) baby;

        eMother.setTimeLastBred(time);
        eFather.setTimeLastBred(time);
        eBaby.setTimeLastBred(time);

    }

    private void dropItemInHand(AllayEntity allay) {
        allay.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        // Send packet to let client know the allay dropped the item
        ServerInstance.get().getPlayerManager().sendToAll(
                new EntityEquipmentUpdateS2CPacket(allay.getId(), List.of(new Pair<>(EquipmentSlot.MAINHAND, ItemStack.EMPTY)))
        );
    }

    @Override
    public void setTimeLastBred(long timeLastBred) {
        this.timeLastBred = timeLastBred;
    }

    @Override
    public boolean isReadyToBreed() {
        AllayEntity allay = AllayEntity.class.cast(this);

        return allay.getWorld().getTime() - timeLastBred > TIME_BETWEEN_BREEDS;
    }
}
