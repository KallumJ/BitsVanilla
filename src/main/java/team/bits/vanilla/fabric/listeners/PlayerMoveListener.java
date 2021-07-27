package team.bits.vanilla.fabric.listeners;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import team.bits.nibbles.event.misc.PlayerMoveEvent;
import team.bits.vanilla.fabric.util.AFKManager;

public class PlayerMoveListener implements PlayerMoveEvent {

    @Override
    public void onPlayerMove(@NotNull PlayerEntity player, @NotNull Vec3d moveVector) {
        AFKManager.playerMoved((ServerPlayerEntity) player);
    }
}
