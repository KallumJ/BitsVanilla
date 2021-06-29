package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtendedPlayerEntity {

    @Accessor()
    @Override
    public abstract PlayerInventory getInventory();

    @Override
    public PlayerEntity self() {
        return this.getInventory().player;
    }
}

