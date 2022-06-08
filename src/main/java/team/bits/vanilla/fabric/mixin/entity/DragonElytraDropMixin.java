package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.boss.dragon.*;
import net.minecraft.item.*;
import net.minecraft.server.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

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
