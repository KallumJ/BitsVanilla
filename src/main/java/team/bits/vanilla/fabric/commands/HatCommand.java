package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.vanilla.fabric.util.ExtendedPlayerEntity;

public class HatCommand extends Command {

    private static final int HEAD_SLOT = 39;

    public HatCommand() {
        super("hat", new String[]{"h"}, new CommandInformation()
            .setDescription("Puts the item in the users hand on their head")
            .setPublic(true)
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ExtendedPlayerEntity ePlayer = (ExtendedPlayerEntity) player;
        Inventory inventory = player.getInventory();
        ItemStack itemInMainHand = player.getMainHandStack();

        // If the player is holding something
        if (itemInMainHand != null) {
            // Add a copy of the item theyre holding to the head slot
            ePlayer.insertItemAtHead(itemInMainHand);

            // Get the slot of the item theyre holding
            int slotOfItemInMainHand = ePlayer.getSlotOfStack(itemInMainHand);

            // If the found slot is not the one we just inserted at the head
            if (slotOfItemInMainHand != HEAD_SLOT) {
                // Remove the original item stack
                inventory.removeStack(slotOfItemInMainHand);
            }
        }
        return 1;
    }
}
