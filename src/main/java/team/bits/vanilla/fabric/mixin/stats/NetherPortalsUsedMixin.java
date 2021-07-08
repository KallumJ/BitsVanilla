package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(Entity.class)
public class NetherPortalsUsedMixin {

    @Inject(
            method = "tickNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"
            )
    )
    public void onNetherPortalUse(CallbackInfo ci) {
        // get the entity who used the portal (this entity)
        Entity entity = Entity.class.cast(this);

        // intellij thinks this cast is impossible, but it's not
        // noinspection ConstantConditions
        if (entity instanceof ServerPlayerEntity player) {

            // increment the nether_portals_used stat by 1
            StatUtils.incrementStat(player, CustomStats.NETHER_PORTALS_USED, 1);
        }
    }
}
