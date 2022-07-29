package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.server.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.server.world.*;
import net.minecraft.text.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;
import team.bits.nibbles.command.*;
import team.bits.nibbles.teleport.*;
import team.bits.nibbles.utils.*;
import team.bits.vanilla.fabric.database.*;
import team.bits.vanilla.fabric.teleport.*;

import java.util.*;
import java.util.concurrent.*;

public class BedCommand extends AsyncCommand {

    private static final String NO_SPAWN_ERR = "You do not have a bed or respawn anchor";
    private static final String RESPAWN_ANCHOR_ERR = "Could not teleport to your respawn anchor";
    private static final String BED_ERR = "Could not teleport to your bed";
    private static final String TELEPORTS_DISABLED = "You have teleporting disabled!";

    public BedCommand() {
        super("bed", new CommandInformation()
                        .setDescription("Teleports you back to your bed")
                        .setPublic(true),
                "b", "home"
        );
    }

    @Override
    public void runAsync(@NotNull CommandContext<ServerCommandSource> context)
            throws InterruptedException, ExecutionException {

        final ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());
        final MinecraftServer server = context.getSource().getServer();

        boolean hasTpDisabled = PlayerApiUtils.getHasTPDisabled(player).get();
        if (!hasTpDisabled) {

            BlockPos spawnPosition = player.getSpawnPointPosition();
            RegistryKey<World> spawnDimension = player.getSpawnPointDimension();
            ServerWorld spawnWorld = server.getWorld(spawnDimension);

            // If a spawn position is found
            if (spawnPosition != null) {

                // Get the spawn positions block
                BlockState blockState = Objects.requireNonNull(spawnWorld).getBlockState(spawnPosition);
                Block block = blockState.getBlock();

                // If spawn position is a bed
                if (block instanceof BedBlock) {
                    handleBedSpawn(player, spawnWorld, spawnPosition);

                    // If spawn position is a respawn anchor
                } else if (isValidRespawnAnchor(blockState, spawnWorld)) {
                    handleRespawnAnchorSpawn(player, spawnWorld, spawnPosition);

                } else {
                    player.sendMessage(Text.literal(NO_SPAWN_ERR).styled(style -> style.withColor(Colors.NEGATIVE)));
                }

            } else {
                player.sendMessage(Text.literal(NO_SPAWN_ERR).styled(style -> style.withColor(Colors.NEGATIVE)));
            }

        } else {
            player.sendMessage(Text.literal(TELEPORTS_DISABLED).styled(style -> style.withColor(Colors.NEGATIVE)));
        }
    }

    private boolean isValidRespawnAnchor(BlockState blockState, World world) {
        return blockState.getBlock() instanceof RespawnAnchorBlock && blockState.get(RespawnAnchorBlock.CHARGES) > 0 && RespawnAnchorBlock.isNether(world);
    }

    private void handleBedSpawn(ServerPlayerEntity player, ServerWorld spawnWorld, BlockPos spawnPos) {
        Optional<Vec3d> bedPos = BedBlock.findWakeUpPosition(EntityType.PLAYER, spawnWorld, spawnPos, 1f);

        if (bedPos.isPresent()) {
            Teleporter.queueTeleport(player, new Location(bedPos.get(), spawnWorld), false);
        } else {
            player.sendMessage(Text.literal(BED_ERR).styled(style -> style.withColor(Colors.NEGATIVE)));
        }
    }

    private void handleRespawnAnchorSpawn(ServerPlayerEntity player, ServerWorld spawnWorld, BlockPos spawnPos) {
        Optional<Vec3d> respawnAnchorPos = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, spawnWorld, spawnPos);
        if (respawnAnchorPos.isPresent()) {
            Teleporter.queueTeleport(player, new Location(respawnAnchorPos.get(), spawnWorld), false);
        } else {
            player.sendMessage(Text.literal(RESPAWN_ANCHOR_ERR).styled(style -> style.withColor(Colors.NEGATIVE)));
        }
    }

}
