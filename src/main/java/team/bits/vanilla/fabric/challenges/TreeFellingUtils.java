package team.bits.vanilla.fabric.challenges;

import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.server.network.*;
import net.minecraft.tag.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import java.util.*;

public class TreeFellingUtils {
    private static final int MIN_LEAF_COUNT = 8;
    private static final int MAX_LOGS_IN_TREE = 160;

    private static final Vec3i[] AREA_AROUND_TRUNK = new Vec3i[]{
            new Vec3i(1, 0, 0), new Vec3i(0, 1, 0),
            new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0),
            new Vec3i(0, 0, -1), new Vec3i(-1, 0, 1),
            new Vec3i(-1, 1, 1), new Vec3i(0, 1, 1),
            new Vec3i(1, 0, 1), new Vec3i(1, 1, 1),
            new Vec3i(1, 1, 0), new Vec3i(1, 0, -1),
            new Vec3i(1, 1, -1), new Vec3i(0, 1, -1),
            new Vec3i(-1, 0, -1), new Vec3i(-1, 1, -1),
            new Vec3i(-1, 1, 0)
    };

    private static final Vec3i[] AREA_AROUND_STUMP = new Vec3i[]{
            new Vec3i(0, 0, 1), new Vec3i(1, 0, 0),
            new Vec3i(1, 0, 1), new Vec3i(-1, 0, 0),
            new Vec3i(-1, 0, -1), new Vec3i(0, 0, -1),
            new Vec3i(-1, 0, 1), new Vec3i(1, 0, -1)
    };

    /**
     * Gets a Tree record, if there is one at the point of the origin
     *
     * @param world  the world
     * @param origin the original block broken, to start checking for a tree from
     * @return a Tree record, if there is a structure that meets tree requirements found, null otherwise
     */
    public static Tree getTree(World world, BlockPos origin) {
        BlockPos initialPos = origin.up();

        List<BlockPos> logPositions = getLogPositionsRecurse(new LinkedList<>(), world, initialPos, new HashSet<>(), new TreeBoundsTracker(origin));

        // If there are enough leaves present, and the logs list is not empty (i.e, was deemed not a tree)
        if (sufficientTouchingLeaves(logPositions, world) && logPositions.size() > 0) {
            // Get stump positions, if this is a 2x2 tree
            Set<BlockPos> stumpPositions = getStumpPositions(origin, world);

            // Return the tree record
            return new Tree(logPositions, stumpPositions, world);
        } else {
            // Else, return null
            return null;
        }

    }

    /**
     * Gets the positions of the stump if this tree is a 2x2
     *
     * @param origin origin of the tree
     * @param world  the world to check in
     * @return the stump positions of the tree
     */
    private static Set<BlockPos> getStumpPositions(BlockPos origin, World world) {
        Set<BlockPos> stumpPositions = new HashSet<>();

        // For every block around the tree
        for (Vec3i vec : AREA_AROUND_STUMP) {
            BlockPos blockNextToOrigin = origin.add(vec);

            // If it is a log, add it to the list
            if (isLogBlock(world.getBlockState(blockNextToOrigin))) {
                stumpPositions.add(blockNextToOrigin);
            }
        }

        return stumpPositions;
    }

    /**
     * Recursively finds all the log positions in a tree
     *
     * @param logPositions     the current list of log positions (starts empty)
     * @param world            the world to check in
     * @param currentPosition  the current position we are visiting
     * @param checkedPositions the positions we have already visited (starts empty)
     * @param treeBounds       a TreeBoundsTracker object to track that the tree never exceeds allowed tree bounds (starts new TreeBoundsTracker(origin)
     * @return a List of log positions, or an empty list if the structure did not adhere to tree limits
     */
    private static List<BlockPos> getLogPositionsRecurse(List<BlockPos> logPositions, World world, BlockPos currentPosition, Set<BlockPos> checkedPositions, TreeBoundsTracker treeBounds) {
        // By visiting this position, we are checking it
        checkedPositions.add(currentPosition);

        // By visiting this position, we know its a log, add it to the tree positions list
        logPositions.add(currentPosition);

        // Update current position so we can track tree bounds
        treeBounds.currentY(currentPosition.getY());
        treeBounds.currentX(currentPosition.getX());
        treeBounds.currentZ(currentPosition.getZ());

        // If we are exceeding the limits of what is considered a tree, return nothing.
        if (treeBounds.exceedsTreeBounds() || logPositions.size() > MAX_LOGS_IN_TREE) {
            logPositions.clear();
            return logPositions;
        }

        // For every space around this log
        for (Vec3i vec : AREA_AROUND_TRUNK) {
            BlockPos currentPos = currentPosition.add(vec);

            // If the space is a log block, and has not already been checked
            if (isLogBlock(world.getBlockState(currentPos)) && !checkedPositions.contains(currentPos)) {

                // Check this log for any further logs
                getLogPositionsRecurse(logPositions, world, currentPos, checkedPositions, treeBounds);
            }

            // This position has now been evaluated
            checkedPositions.add(currentPos);
        }

        return logPositions;
    }

    /**
     * Gets whether the provided block is a log
     *
     * @param blockState the BlockState of the block to check
     * @return true if a log, false otherwise
     */
    public static boolean isLogBlock(BlockState blockState) {
        return blockState.getRegistryEntry().isIn(BlockTags.LOGS);
    }

    /**
     * Gets whether the provided block is a leaf
     *
     * @param blockState the BlockState of the block to check
     * @return true if a leaf, false otherwise
     */
    private static boolean isLeafBlock(BlockState blockState) {
        return blockState.getRegistryEntry().isIn(BlockTags.LEAVES);
    }

    /**
     * Finds whether this tree should be felled
     *
     * @param player the player to check
     * @return true if the conditions are met for felling, false otherwise
     */
    public static boolean playerMeetsFellingConditions(ServerPlayerEntity player) {
        ItemStack axe = player.getMainHandStack();

        int isTreeFellerAxe = 0;
        if (axe.getNbt() != null) {
            isTreeFellerAxe = axe.getNbt().getInt(ChallengeRewardItems.TREE_FELLER_NBT);
        }

        return player.isSneaking() && isTreeFellerAxe == 1;
    }

    /**
     * Finds whether there are sufficient leaves touching the logs
     *
     * @param logPositions the logs to check
     * @param world        the world to check in
     * @return true if there are enough leaves touching, false otherwise
     */
    private static boolean sufficientTouchingLeaves(List<BlockPos> logPositions, World world) {
        int leafCount = 0;

        Set<BlockPos> checkedPositions = new HashSet<>();

        // For every log position in the tree
        for (BlockPos logPos : logPositions) {
            // For every position around the log
            for (Vec3i vec : AREA_AROUND_TRUNK) {
                BlockPos currentPos = logPos.add(vec);
                // If this position is not checked
                if (!checkedPositions.contains(currentPos)) {
                    // If it is a leaf, increment the leaf count, and track the latest touching leaf
                    BlockState blockState = world.getBlockState(currentPos);
                    if (isLeafBlock(blockState)) {
                        leafCount++;
                    }
                    checkedPositions.add(currentPos);
                }
            }

            // If our leaf count exceeds min leaf count, we do not need to check anymore.
            if (leafCount >= MIN_LEAF_COUNT) {
                break;
            }
        }


        return leafCount >= MIN_LEAF_COUNT;
    }
}

/**
 * A class to track we do not exceed the allowed bounds of a tree
 */
class TreeBoundsTracker {
    private static final int MAX_BRANCH_LENGTH = 7;
    private static final int MAX_TREE_HEIGHT = 31;

    private int xDiff;
    private int yDiff;
    private int zDiff;

    private final int originX;
    private final int originY;
    private final int originZ;

    public TreeBoundsTracker(BlockPos pos) {
        originX = Math.abs(pos.getX());
        originY = Math.abs(pos.getY());
        originZ = Math.abs(pos.getZ());
    }

    /**
     * Notifies the tracker of the current x co-ord
     *
     * @param x the x co-ord
     */
    public void currentX(int x) {
        int absX = Math.abs(x) - Math.abs(originX);

        if (absX > this.xDiff) {
            this.xDiff = absX;
        }
    }

    /**
     * Notifies the tracker of the current y co-ord
     *
     * @param y the x co-ord
     */
    public void currentY(int y) {
        int absY = Math.abs(y) - Math.abs(originY);

        if (absY > this.yDiff) {
            this.yDiff = absY;
        }
    }

    /**
     * Notifies the tracker of the current z co-ord
     *
     * @param z the x co-ord
     */
    public void currentZ(int z) {
        int absZ = Math.abs(z) - Math.abs(originZ);

        if (absZ > this.zDiff) {
            this.zDiff = absZ;
        }
    }

    /**
     * Checks whether we are exceeding the bounds of a tree
     *
     * @return true if we are exceeding tree bounds, false otherwise
     */
    public boolean exceedsTreeBounds() {
        boolean exceedTreeHeight = yDiff >= MAX_TREE_HEIGHT;
        boolean exceedBranchSize = xDiff >= MAX_BRANCH_LENGTH || zDiff >= MAX_BRANCH_LENGTH;

        return exceedTreeHeight || exceedBranchSize;
    }
}