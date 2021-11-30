package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public abstract class DragonElytraDropMixin {

    private static final int DROP_CHANCE = 30;

    @Shadow
    public abstract boolean hasPreviouslyKilled();

    @Inject(
            method = "dragonKilled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/boss/ServerBossBar;setPercent(F)V"
            )
    )
    public void onDragonKilled(EnderDragonEntity dragon, CallbackInfo ci) {
        final ServerWorld world = (ServerWorld) dragon.world;
        if (this.hasPreviouslyKilled()) {
            if (world.random.nextInt(100) < DROP_CHANCE) {
                ItemEntity elytraItemEntity = new ItemEntity(
                        world, dragon.getX(), dragon.getY(), dragon.getZ(),
                        new ItemStack(Items.ELYTRA, 1)
                );
                world.spawnEntity(elytraItemEntity);
            }
        }
    }
}
