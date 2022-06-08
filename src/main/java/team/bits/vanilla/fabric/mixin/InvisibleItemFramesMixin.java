package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionEntity.class)
public class InvisibleItemFramesMixin {
    @Inject(
            method = "onCollision",
            at = @At("TAIL")
    )
    public void onBlockHit(HitResult hitResult, CallbackInfo ci) {
        PotionEntity potionEntity = (PotionEntity) (Object) this;
        ItemStack potion = potionEntity.getStack();


        if (isPotionInvisibility(potion)) {
            Vec3d blockHit = hitResult.getPos();

            List<ItemFrameEntity> itemFrames = potionEntity.getWorld().getEntitiesByType(
                    TypeFilter.instanceOf(ItemFrameEntity.class),
                    new Box(blockHit, blockHit),
                    entity -> true
            );

            for (ItemFrameEntity itemFrame : itemFrames) {
                itemFrame.setInvisible(!itemFrame.isInvisible());
            }
        }
    }

    private boolean isPotionInvisibility(ItemStack potion) {
        List<StatusEffectInstance> statusEffects = PotionUtil.getPotionEffects(potion);

        for (StatusEffectInstance statusEffect : statusEffects) {
            if (statusEffect.getEffectType().equals(StatusEffects.INVISIBILITY)) {
                return true;
            }
        }

        return false;
    }
}
