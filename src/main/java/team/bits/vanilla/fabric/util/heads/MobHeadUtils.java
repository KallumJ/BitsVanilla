package team.bits.vanilla.fabric.util.heads;

import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class MobHeadUtils {
    public static @NotNull ItemStack getHeadForPlayer(String username) {
        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD, 1);
        playerHead.setSubNbt("SkullOwner", NbtString.of(username));

        return playerHead;
    }

    public static Optional<MobHead> getHeadForEntity(LivingEntity entity) {
        List<MobHead> heads = MobHeads.getMobHeadsForEntityType(entity.getType());

        Optional<MobHead> selectedHead = Optional.empty();
        // For every head registered to this type
        for (MobHead head : heads) {
            // If this entity passes the test required for the head, select it
            if (head.test(entity)) {
                selectedHead = Optional.of(head);
                break;
            }
        }

        return selectedHead;
    }
}
