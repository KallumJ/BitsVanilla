package team.bits.vanilla.fabric.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.bits.vanilla.fabric.event.sleep.PlayerSleepCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerWakeUpCallback;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class SleepMixin {

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;updateSleepingPlayers()V"),
            method = "trySleep"
    )
    private void onSleep(BlockPos pos,
                         CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {
        PlayerEntity player = ((ExtendedPlayerEntity) this).self();
        PlayerSleepCallback.EVENT.invoker().onSleep(player);
    }

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;wakeUp(ZZ)V"),
            method = "wakeUp"
    )
    private void onWakeUp(boolean bl, boolean updateSleepingPlayers, CallbackInfo ci) {
        PlayerEntity player = ((ExtendedPlayerEntity) this).self();
        PlayerWakeUpCallback.EVENT.invoker().onWakeUp(player);
    }

}
