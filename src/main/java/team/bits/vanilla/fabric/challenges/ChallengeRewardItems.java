package team.bits.vanilla.fabric.challenges;

import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import team.bits.nibbles.utils.*;

import java.util.*;

public class ChallengeRewardItems {

    public static final ItemStack PROSPEROUS_PICKAXE = createProsperousPickaxe();
    public static final ItemStack INSTAMINER = createInstaminer();
    public static final ItemStack TREECAPITATOR = createTreecapitator();
    public static final ItemStack EVERLASTING_WINGS = createEverlastingWings();
    public static final ItemStack RUNNING_BOOTS = createRunningBoots();
    public static final ItemStack NEPTUNE = createNeptuneTrident();

    public static final String RUNNING_BOOTS_NBT = "running_boots";
    public static final String TREE_FELLER_NBT = "tree_feller";

    private static ItemStack createNeptuneTrident() {
        final Text name = createItemName("Neptune");
        final List<Text> tooltip = List.of(Text.empty(), Text.literal("Aquaman's ol' reliable"));
        final List<Enchantment> enchantments = List.of(
                new Enchantment(Enchantments.RIPTIDE, 3),
                new Enchantment(Enchantments.IMPALING, 5),
                new Enchantment(Enchantments.UNBREAKING, 3)
        );
        final Item item = Items.TRIDENT;

        return createEnchantedItem(item, enchantments, name, tooltip);
    }

    private static ItemStack createRunningBoots() {
        final Text name = createItemName("Air Jordan Yeezy Pros");
        final List<Text> tooltip = List.of(
                Text.empty(),
                Text.literal("Run Forrest Run ;)"),
                Text.literal("*speed 2 while sprinting*")
                        .styled(style -> style.withColor(Formatting.GRAY).withItalic(true))
        );
        final List<Enchantment> enchantments = List.of(
                new Enchantment(Enchantments.MENDING, 1),
                new Enchantment(Enchantments.SOUL_SPEED, 4)
        );
        final Item item = Items.DIAMOND_BOOTS;

        ItemStack itemStack = createEnchantedItem(item, enchantments, name, tooltip);
        itemStack.setSubNbt(RUNNING_BOOTS_NBT, NbtInt.of(1));

        return itemStack;
    }

    private static ItemStack createEverlastingWings() {
        final Text name = createItemName("Everlasting Wings");
        final List<Text> tooltip = List.of(Text.empty(), Text.literal("Flap forever"));
        final List<Enchantment> enchantments = List.of(
                new Enchantment(Enchantments.MENDING, 1),
                new Enchantment(Enchantments.UNBREAKING, 5)
        );
        final Item item = Items.ELYTRA;

        return createEnchantedItem(item, enchantments, name, tooltip);
    }

    private static ItemStack createTreecapitator() {
        final Text name = createItemName("Treecapitator");
        final List<Text> tooltip = List.of(
                Text.empty(),
                Text.literal("TIMBER!"),
                Text.literal("*sneak and cut down tree*")
                        .styled(style -> style.withColor(Formatting.GRAY).withItalic(true))
        );
        final List<Enchantment> enchantments = List.of(
                new Enchantment(Enchantments.MENDING, 1)
        );
        final Item item = Items.DIAMOND_AXE;

        ItemStack itemStack = createEnchantedItem(item, enchantments, name, tooltip);
        itemStack.setSubNbt(TREE_FELLER_NBT, NbtInt.of(1));

        return itemStack;
    }

    private static ItemStack createInstaminer() {
        final Text name = createItemName("Instaminer");
        final List<Text> tooltip = List.of(Text.empty(), Text.literal("No beacon required!"));
        final List<Enchantment> enchantments = List.of(
                new Enchantment(Enchantments.EFFICIENCY, 8), // Level 8, instamines stone, instamines deepslate with haste 2, doesnt instamine deepslate ores
                new Enchantment(Enchantments.UNBREAKING, 3),
                new Enchantment(Enchantments.MENDING, 1)
        );
        final Item item = Items.DIAMOND_PICKAXE;

        return createEnchantedItem(item, enchantments, name, tooltip);
    }

    private static ItemStack createProsperousPickaxe() {
        final Text name = createItemName("Prosperous Pickaxe");
        final List<Text> tooltip = List.of(Text.empty(), Text.literal("Fabricates fortunes!"));
        final List<Enchantment> enchantments = List.of(
                new Enchantment(Enchantments.FORTUNE, 5),
                new Enchantment(Enchantments.MENDING, 1)
        );
        final Item item = Items.DIAMOND_PICKAXE;

        return createEnchantedItem(item, enchantments, name, tooltip);
    }

    private static ItemStack createEnchantedItem(Item item, List<Enchantment> enchantments,
                                                 Text name, List<Text> tooltip) {
        ItemStack itemStack = new ItemStack(item);

        if (!enchantments.isEmpty()) {
            for (Enchantment enchantment : enchantments) {
                itemStack.addEnchantment(enchantment.enchantment(), enchantment.level());
            }
        }

        if (!tooltip.isEmpty()) {
            ItemUtils.setLore(itemStack, tooltip);
        }

        if (name != null) {
            itemStack.setCustomName(name);
        }

        return itemStack;
    }

    private static Text createItemName(String name) {
        return Text.literal(name).styled(style -> style.withColor(Formatting.GOLD).withItalic(false));
    }
}

record Enchantment(net.minecraft.enchantment.Enchantment enchantment, int level) {
}
