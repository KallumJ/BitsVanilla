package team.bits.vanilla.fabric.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow
    private int pickupDelay;

    @Shadow
    public abstract ItemStack getStack();

    @Overwrite
    public boolean cannotPickup() {
        return this.pickupDelay > 0 || this.getStack().isOf(Items.EGG);
    }

}
