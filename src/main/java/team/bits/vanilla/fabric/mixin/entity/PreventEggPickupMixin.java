package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.*;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.*;

@Mixin(ItemEntity.class)
public abstract class PreventEggPickupMixin {

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
