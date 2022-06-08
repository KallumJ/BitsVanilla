package team.bits.vanilla.fabric.mixin.challenges;

import net.minecraft.entity.*;
import net.minecraft.entity.boss.*;
import net.minecraft.entity.boss.dragon.*;
import net.minecraft.entity.damage.*;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import team.bits.vanilla.fabric.challenges.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;

@Mixin(LivingEntity.class)
public class WitherDragonMixin {

    private static final Set<WitherDragonRecord> WITHER_DRAGON_RECORDS = new HashSet<>();

    private static @NotNull Optional<WitherDragonRecord> getWitherDragonRecord(@NotNull ServerPlayerEntity player) {
        return WITHER_DRAGON_RECORDS.stream()
                .filter(record -> record.player().equals(player))
                .findFirst();
    }

    @Inject(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    // we target Entity#setPose because this call is made at the
                    // end of the onDeath call, but still inside the `if` block.
                    target = "Lnet/minecraft/entity/LivingEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"
            )
    )
    public void onLivingEntityDeath(DamageSource damageSource, CallbackInfo ci) {
        if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {
            long currentTime = player.getWorld().getTime();
            Optional<WitherDragonRecord> record = getWitherDragonRecord(player);
            if (record.isEmpty()) {
                this.tryAddWitherDragonRecord(player);
            } else {
                if (currentTime > record.get().time() + (30 * 20)) {
                    WITHER_DRAGON_RECORDS.remove(record.get());
                    this.tryAddWitherDragonRecord(player);
                } else {
                    this.tryCompleteWitherDragonRecord(record.get());
                }
            }
        }
    }

    private void tryAddWitherDragonRecord(@NotNull ServerPlayerEntity player) {
        long currentTime = player.getWorld().getTime();
        LivingEntity target = (LivingEntity) (Object) this;
        if (target instanceof EnderDragonEntity) {
            WITHER_DRAGON_RECORDS.add(
                    new WitherDragonRecord(player, currentTime, false, true)
            );
        } else if (target instanceof WitherEntity) {
            WITHER_DRAGON_RECORDS.add(
                    new WitherDragonRecord(player, currentTime, true, false)
            );
        }
    }

    private void tryCompleteWitherDragonRecord(@NotNull WitherDragonRecord record) {
        LivingEntity target = (LivingEntity) (Object) this;
        if ((!record.dragonKilled() && target instanceof EnderDragonEntity) ||
                (!record.witherKilled() && target instanceof WitherEntity)) {

            ((ExtendedPlayerEntity) record.player()).markChallengeCompleted(Challenges.WITHER_DRAGON);
            WITHER_DRAGON_RECORDS.remove(record);
        }
    }
}
