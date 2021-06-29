package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.bits.vanilla.fabric.event.sleep.PlayerMoveCallback;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtendedPlayerEntity {

    private Vec3d previousPos = Vec3d.ZERO;

    @Accessor()
    @Override
    public abstract PlayerInventory getInventory();

    @Override
    public PlayerEntity self() {
        return this.getInventory().player;
    }

    @Inject(
            method = "tickMovement",
            at = @At(value = "TAIL")
    )
    private void onWakeUp(CallbackInfo ci) {
        PlayerEntity player = PlayerEntity.class.cast(this);
        Vec3d currentPos = player.getPos();
        Vec3d moveVector = this.previousPos.subtract(currentPos);

        if (moveVector.length() > 0.1) {
            PlayerMoveCallback.EVENT.invoker().onPlayerMove(player, moveVector);
        }

        this.previousPos = currentPos;
    }
}

