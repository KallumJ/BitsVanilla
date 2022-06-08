package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.decoration.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(ArmorStandEntity.class)
public interface ExtendedArmorStand {

    @Accessor("disabledSlots")
    void e_setDisabledSlots(int disabledSlots);

    @Invoker("setShowArms")
    void e_setShowArms(boolean showArms);

    @Invoker("setHideBasePlate")
    void e_setHideBasePlate(boolean hideBasePlate);
}
