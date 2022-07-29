package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.utils.*;

public class HatCommand extends Command {

    private static final String NO_ITEM_ERR = "You don't have an item in your hand";
    private static final String HAT_EQUIPPED = "Enjoy your new hat!";

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
        if (stackInMainHand != null && !stackInMainHand.isEmpty()) {

            // get a copy of the item on the player's head
            ItemStack oldHat = player.getEquippedStack(EquipmentSlot.HEAD).copy();

            // Add the stack they're holding to the head slot
            player.equipStack(EquipmentSlot.HEAD, stackInMainHand);

            // replace the item in the selected slot (main hand) with their old hat
            inventory.setStack(inventory.selectedSlot, oldHat);

            // send a confirmation message
            player.sendMessage(Text.literal(HAT_EQUIPPED).styled(style -> style.withColor(Colors.POSITIVE)));

        } else {
            // if the player isn't holding an item, send an error message
            throw new SimpleCommandExceptionType(() -> NO_ITEM_ERR).create();
        }

        return 1;
    }
}
