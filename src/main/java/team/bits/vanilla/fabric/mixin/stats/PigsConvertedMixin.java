package team.bits.vanilla.fabric.mixin.stats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.statistics.lib.CustomStats;
import team.bits.vanilla.fabric.statistics.lib.StatUtils;

@Mixin(PigEntity.class)
public class PigsConvertedMixin {

    @Inject(
            method = "onStruckByLightning",
            at = @At("HEAD")
    )
    public void onPigConvert(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getWorld().equals(world) && player.distanceTo((Entity) (Object) this) < 10) {
                StatUtils.incrementStat(player, CustomStats.PIGS_CONVERTED, 1);
            }
        }
    }
}
