package team.bits.vanilla.fabric.listeners;

import net.minecraft.block.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.event.interaction.*;
import team.bits.vanilla.fabric.challenges.*;

public class TreeFellingListener implements PlayerBlockBreakEvent.Listener {
    @Override
    public void onBlockBroken(@NotNull PlayerBlockBreakEvent event) {
        BlockState blockState = event.getBlockState();
        ServerPlayerEntity player = event.getPlayer();
        ServerWorld world = event.getWorld();
        BlockPos origin = event.getPos();

        // If the player meets felling conditions
        if (TreeFellingUtils.playerMeetsFellingConditions(player)) {
            // If the block is a log
            if (TreeFellingUtils.isLogBlock(blockState)) {

                // Get tree from this position
                Tree tree = TreeFellingUtils.getTree(world, origin);

                // If a suitable tree is found, destroy it
                if (tree != null) {
                    tree.destroyTree(player.getMainHandStack(), player);
                }
            }
        }
    }

}
