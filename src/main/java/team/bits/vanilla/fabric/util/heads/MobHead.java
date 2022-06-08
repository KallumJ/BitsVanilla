package team.bits.vanilla.fabric.util.heads;

import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import team.bits.vanilla.fabric.util.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public record MobHead(EntityType<?> entityType, String name, String uuid,
                      String texture,
                      Predicate<LivingEntity> predicate, double chance,
                      double lootMult) implements Predicate<LivingEntity> {

    /*
        Code adapted from here, distributed under the MIT license
        https://github.com/ricksouth/serilum-mc-mods
    */
    public ItemStack toItemStack() {
        ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD, 1);

        List<Integer> uuidIntArray = UUIDUtils.uuidToIntArray(uuid);

        NbtCompound skullOwner = new NbtCompound();
        skullOwner.putIntArray("Id", uuidIntArray);

        NbtCompound properties = new NbtCompound();
        NbtList textures = new NbtList();
        NbtCompound tex = new NbtCompound();
        tex.putString("Value", texture);
        textures.add(tex);

        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        playerHead.setSubNbt("SkullOwner", skullOwner);


        Text displayName = Text.literal(name + "'s Head")
                .styled(style -> style.withColor(Formatting.YELLOW).withItalic(false));

        playerHead.setCustomName(displayName);

        return playerHead;
    }

    @Override
    public boolean test(LivingEntity entity) {
        // If this head doesn't have a test, then the entity can be assumed as passing the test
        if (predicate == null) {
            return true;
        }

        // Else run the test
        return predicate.test(entity);
    }

    public boolean shouldDrop(ItemStack killingItem) {
        // Get the looting level
        int lootingLevel = 0;
        if (!killingItem.hasEnchantments()) {
            lootingLevel = EnchantmentHelper.getLevel(Enchantments.LOOTING, killingItem);
        }

        // Return whether the head should drop this time
        return ThreadLocalRandom.current().nextDouble() < chance + (lootingLevel * lootMult);
    }
}
