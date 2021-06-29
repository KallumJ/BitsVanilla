package team.bits.vanilla.fabric.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

public interface ExtendedPlayerEntity {

    PlayerInventory getInventory();

    PlayerEntity self();
}
