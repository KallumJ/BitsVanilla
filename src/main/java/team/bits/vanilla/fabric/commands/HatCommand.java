package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;

public class HatCommand extends Command {

    public HatCommand() {
        super("hat", new CommandInformation()
                        .setDescription("Puts the item in the users hand on their head")
                        .setPublic(true),
                "h"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ItemStack stackInMainHand = player.getMainHandStack();
        PlayerInventory inventory = player.getInventory();

        // If the player is holding something
        if (stackInMainHand != null) {

            // get a copy of the item on the player's head
            ItemStack oldHat = player.getEquippedStack(EquipmentSlot.HEAD).copy();

            // Add the stack they're holding to the head slot
            player.equipStack(EquipmentSlot.HEAD, stackInMainHand);

            // replace the item in the selected slot (main hand) with their old hat
            inventory.setStack(inventory.selectedSlot, oldHat);
        }
        return 1;
    }
}
