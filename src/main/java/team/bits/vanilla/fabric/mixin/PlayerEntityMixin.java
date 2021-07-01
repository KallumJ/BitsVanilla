package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.event.damage.PlayerDamageCallback;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    private Vec3d previousPos = Vec3d.ZERO;

    @Inject(
            method = "tickMovement",
            at = @At(value = "TAIL")
    )
    private void onTickMovement(CallbackInfo ci) {
        PlayerEntity player = PlayerEntity.class.cast(this);
        Vec3d currentPos = player.getPos();
        Vec3d moveVector = this.previousPos.subtract(currentPos);

        if (moveVector.length() > 0.1) {
            PlayerMoveCallback.EVENT.invoker().onPlayerMove(player, moveVector);
        }

        this.previousPos = currentPos;
    }

    @Inject(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"
            )
    )
    private void onDamage(DamageSource source, float amount, CallbackInfo ci) {
        PlayerEntity player = PlayerEntity.class.cast(this);
        PlayerDamageCallback.EVENT.invoker().onPlayerDamage(player, source, amount);
    }

}

