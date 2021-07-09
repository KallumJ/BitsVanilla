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

    /**
     * Prevents entities picking up items if they are an egg
     *
     * @return true if item can be picked up, false if not
     * @author Kallum
     */
    @Overwrite
    public boolean cannotPickup() {
        return this.pickupDelay > 0 || this.getStack().isOf(Items.EGG);
    }

}
