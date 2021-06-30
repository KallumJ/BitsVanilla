package team.bits.vanilla.fabric.util;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerWrapper {

    private ServerPlayerEntity player;

    public PlayerWrapper(ServerPlayerEntity player) {
        this.player = player;
    }

    public boolean checkPlayerHasItem(Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isOf(item)) {
                return true;
            }
        }
        return false;
    }

    public int getSlotOfItem(Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    public void giveItem(ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();
        int occupiedSlot = inventory.getOccupiedSlotWithRoomForStack(itemStack);

        if (occupiedSlot != -1) {
            inventory.insertStack(occupiedSlot, itemStack);
        } else {
            inventory.insertStack(inventory.getEmptySlot(), itemStack);
        }
    }

    public void removeItem(int slot, int amount) {
        PlayerInventory inventory = player.getInventory();
        inventory.removeStack(slot, amount);
    }
}
