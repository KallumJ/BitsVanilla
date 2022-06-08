package team.bits.vanilla.fabric.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MobSilenceMixin {
    private static final String NAME_TO_SILENCE = "silence me";

    @Inject(
            method = "playSound",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onPlaySound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        Text customName = entity.getCustomName();
        if (customName != null) {
            String nameStr = customName.getString();
            if (nameStr.equalsIgnoreCase(NAME_TO_SILENCE)) {
                ci.cancel();
            }
        }
    }

}
