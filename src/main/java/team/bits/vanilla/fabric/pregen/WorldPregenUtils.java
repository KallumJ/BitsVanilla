package team.bits.vanilla.fabric.pregen;

import com.mojang.datafixers.util.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.*;
import org.jetbrains.annotations.*;
import team.bits.vanilla.fabric.mixin.pregen.*;

import java.util.concurrent.*;

public final class WorldPregenUtils {

    private static final ChunkTicketType<Unit> TICKET_TYPE = ChunkTicketType.create(
            "bits_pregen", (unit, unit2) -> 0
    );

    private WorldPregenUtils() {
    }

    public static CompletableFuture<Void> generateChunkAsync(@NotNull ServerWorld world, int x, int z) {
        // if we're not on the main server thread, run this function as a callback on the main server thread
        if (Thread.currentThread() != world.getServer().getThread()) {
            return CompletableFuture.supplyAsync(() -> generateChunkAsync(world, x, z), world.getServer()).join();
        } else {
            if (isChunkGenerated(world, x, z)) {
                return CompletableFuture.completedFuture(null);
            }

            ChunkPos chunkPos = new ChunkPos(x, z);
            ServerChunkManager serverChunkManager = world.getChunkManager();
            serverChunkManager.addTicket(TICKET_TYPE, chunkPos, 0, Unit.INSTANCE);
            ((ServerChunkManagerMixin) serverChunkManager).c_tick();
            ThreadedAnvilChunkStorage threadedAnvilChunkStorage = serverChunkManager.threadedAnvilChunkStorage;
            ThreadedAnvilChunkStorageMixin threadedAnvilChunkStorageMixin = (ThreadedAnvilChunkStorageMixin) threadedAnvilChunkStorage;
            ChunkHolder chunkHolder = threadedAnvilChunkStorageMixin.c_getChunkHolder(chunkPos.toLong());
            CompletableFuture<Void> chunkFuture = chunkHolder == null ? CompletableFuture.completedFuture(null) : CompletableFuture.allOf(chunkHolder.getChunkAt(ChunkStatus.FULL, threadedAnvilChunkStorage));
            chunkFuture.whenCompleteAsync((ignored, throwable) -> serverChunkManager.removeTicket(TICKET_TYPE, chunkPos, 0, Unit.INSTANCE), world.getServer());
            return chunkFuture;
        }
    }

    private static boolean isChunkGenerated(@NotNull ServerWorld world, int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        ThreadedAnvilChunkStorage chunkStorage = world.getChunkManager().threadedAnvilChunkStorage;
        ThreadedAnvilChunkStorageMixin chunkStorageMixin = (ThreadedAnvilChunkStorageMixin) chunkStorage;
        ChunkHolder loadedChunkHolder = chunkStorageMixin.c_getChunkHolder(chunkPos.toLong());
        if (loadedChunkHolder != null && loadedChunkHolder.getCurrentStatus() == ChunkStatus.FULL) {
            return true;
        }
        ChunkHolder unloadedChunkHolder = chunkStorageMixin.c_getChunksToUnload().get(chunkPos.toLong());
        return unloadedChunkHolder != null && unloadedChunkHolder.getCurrentStatus() == ChunkStatus.FULL;
    }
}
