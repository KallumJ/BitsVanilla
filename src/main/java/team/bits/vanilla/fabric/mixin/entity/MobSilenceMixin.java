package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MobSilenceMixin {
    private static final String NAME_TO_SILENCE = "silence me";

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void onTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        Text customName = entity.getCustomName();

        boolean validName = false;
        if (customName != null) {
            String nameStr = customName.getString();
            if (nameStr.equalsIgnoreCase(NAME_TO_SILENCE)) {
                validName = true;
            }
        }

        entity.setSilent(validName);
    }
}
