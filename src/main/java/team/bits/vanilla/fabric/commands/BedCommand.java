package team.bits.vanilla.fabric.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import team.bits.nibbles.command.Command;
import team.bits.nibbles.command.CommandInformation;
import team.bits.nibbles.teleport.Location;
import team.bits.vanilla.fabric.teleport.Teleporter;

import java.util.Objects;
import java.util.Optional;

public class BedCommand extends Command {

    private static final String NO_SPAWN_ERR = "You do not have a bed or respawn anchor";
    private static final String RESPAWN_ANCHOR_ERR = "Could not teleport to your respawn anchor";
    private static final String BED_ERR = "Could not teleport to your bed";

    public BedCommand() {
        super("bed", new CommandInformation()
                        .setDescription("Teleports you back to your bed")
                        .setPublic(true),
                "b", "home"
        );
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MinecraftServer server = context.getSource().getServer();

        BlockPos spawnPosition = player.getSpawnPointPosition();
        RegistryKey<World> spawnDimension = player.getSpawnPointDimension();
        ServerWorld spawnWorld = server.getWorld(spawnDimension);

        // If no spawn position is found
        if (spawnPosition != null) {

            // Get the spawn positions block
            BlockState blockState = Objects.requireNonNull(spawnWorld).getBlockState(spawnPosition);
            Block block = blockState.getBlock();

            // If spawn position is a bed
            if (block instanceof BedBlock) {
                Optional<Vec3d> bedPos = BedBlock.findWakeUpPosition(EntityType.PLAYER, spawnWorld, spawnPosition, 1f);

                if (bedPos.isPresent()) {
                    Teleporter.queueTeleport(player, new Location(bedPos.get(), spawnWorld), false);
                } else {
                    throw new SimpleCommandExceptionType(() -> BED_ERR).create();
                }

                // If spawn position is a respawn anchor
            } else if (block instanceof RespawnAnchorBlock && blockState.get(RespawnAnchorBlock.CHARGES) > 0 && RespawnAnchorBlock.isNether(spawnWorld)) {
                Optional<Vec3d> respawnAnchorPos = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, spawnWorld, spawnPosition);
                if (respawnAnchorPos.isPresent()) {
                    Teleporter.queueTeleport(player, new Location(respawnAnchorPos.get(), spawnWorld), false);
                } else {
                    throw new SimpleCommandExceptionType(() -> RESPAWN_ANCHOR_ERR).create();
                }
            } else {
                throw new SimpleCommandExceptionType(() -> NO_SPAWN_ERR).create();
            }
        } else {
            throw new SimpleCommandExceptionType(() -> NO_SPAWN_ERR).create();
        }

        return 1;
    }
}
