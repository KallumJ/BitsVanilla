package team.bits.vanilla.fabric.challenges;

import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.sound.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import team.bits.nibbles.player.*;
import team.bits.nibbles.utils.*;

import java.util.*;

public class Tree {
    private final List<BlockPos> logPositions;
    private final Set<BlockPos> stumpPositions;
    private final World world;

    private boolean soundPlayed;
    private static final Text BREAK_MSG = Text.literal("Careful! Your axe is about to break!").styled(style -> style.withColor(Colors.NEGATIVE));

    public Tree(List<BlockPos> logPositions, Set<BlockPos> stumpPositions, World world) {
        this.logPositions = logPositions;
        this.stumpPositions = stumpPositions;
        this.world = world;
    }

    public void destroyTree(ItemStack axe, ServerPlayerEntity player) {
        for (BlockPos logPosition : logPositions) {
            breakLog(axe, logPosition, player);
        }

        for (BlockPos stumpPosition : stumpPositions) {
            breakLog(axe, stumpPosition, player);
        }
    }

    private void breakLog(ItemStack axe, BlockPos pos, ServerPlayerEntity player) {

        if (axe.getItemBarStep() > 1) {
            world.breakBlock(pos, true);
            axe.damage(1, player.getRandom(), player);
        }

        if (axe.getItemBarStep() == 1 && !soundPlayed) {
            INibblesPlayer nPlayer = (INibblesPlayer) player;
            nPlayer.playSound(SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1.0f, 1.5f);
            player.sendMessage(BREAK_MSG);
            soundPlayed = true;
        }
    }
}
