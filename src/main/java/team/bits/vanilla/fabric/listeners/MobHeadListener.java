package team.bits.vanilla.fabric.listeners;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.interaction.PlayerKillEntityEvent;
import team.bits.vanilla.fabric.util.heads.MobHead;
import team.bits.vanilla.fabric.util.heads.MobHeadUtils;

import java.util.Optional;

public class MobHeadListener implements PlayerKillEntityEvent.Listener {
    @Override
    public void onPlayerKillEntity(@NotNull PlayerKillEntityEvent event) {
        LivingEntity entityKilled = event.getKilledEntity();
        ServerPlayerEntity player = event.getPlayer();

        Optional<MobHead> optHead = MobHeadUtils.getHeadForEntity(entityKilled);
        if (optHead.isPresent()) {
            MobHead foundHead = optHead.get();

            ItemStack itemInHand = player.getMainHandStack();

            if (foundHead.shouldDrop(itemInHand)) {
                entityKilled.dropStack(foundHead.toItemStack());
            }
        }
    }
}
