package team.bits.vanilla.fabric.listeners;

import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.interaction.*;
import team.bits.vanilla.fabric.challenges.*;

public class RunningBootsRunListener implements PlayerMoveEvent.Listener {
    @Override
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        PlayerEntity player = event.getPlayer();

        if (player.isSprinting()) {
            if (isPlayerWearingBoots(player)) {
                player.addStatusEffect(
                        new StatusEffectInstance(
                                StatusEffects.SPEED, 3, 1,
                                false, false
                        )
                );
            }
        }
    }

    private boolean isPlayerWearingBoots(PlayerEntity player) {
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);

        if (boots != null) {
            if (boots.getNbt() != null) {
                return boots.getNbt().getInt(ChallengeRewardItems.RUNNING_BOOTS_NBT) == 1;
            }
        }

        return false;
    }
}
