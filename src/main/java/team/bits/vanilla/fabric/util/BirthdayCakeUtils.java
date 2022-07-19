package team.bits.vanilla.fabric.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import team.bits.nibbles.teleport.Location;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static net.minecraft.block.CakeBlock.BITES;

public class BirthdayCakeUtils {
    public static final BlockState BIRTHDAY_CAKE_STATE = CandleCakeBlock.getCandleCakeFromCandle(Block.getBlockFromItem(Items.LIGHT_BLUE_CANDLE));

    private static Set<Location> birthdayCakes = Collections.emptySet();

    public static Set<Location> getBirthdayCakes() {
        return Collections.unmodifiableSet(birthdayCakes);
    }

    public static void setBirthdayCakes(Set<Location> birthdayCakes) {
        BirthdayCakeUtils.birthdayCakes = new LinkedHashSet<>(birthdayCakes);
    }

    public static void addBirthdayCake(Location location) {
        birthdayCakes.add(location);
    }

    public static boolean isLocationBirthdayCake(Location location) {
        for (Location birthdayCake : birthdayCakes) {
            if (birthdayCake.equals(location)) {
                return true;
            }
        }
        return false;
    }

    public static void removeBirthdayCake(Location location) {
        birthdayCakes.remove(location);
    }


    public static boolean isCakeUnfinished(BlockState cakeState) {
        if (cakeState.isIn(BlockTags.CANDLE_CAKES)) {
            return true;
        }

        if (cakeState.isOf(Blocks.CAKE)) {
            int bitesTaken = cakeState.get(BITES);
            return bitesTaken < 6;
        }

        return false;
    }

    public static boolean isCake(BlockState blockState) {
        return blockState.isIn(BlockTags.CANDLE_CAKES) || blockState.isOf(Blocks.CAKE);
    }
}
