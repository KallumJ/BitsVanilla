package team.bits.vanilla.fabric.mixin.pregen;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.nbt.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("UnnecessaryInterfaceModifier")
@Mixin(ThreadedAnvilChunkStorage.class)
public interface ThreadedAnvilChunkStorageMixin {

    @Invoker("getChunkHolder")
    public ChunkHolder c_getChunkHolder(long pos);

    @Invoker("getUpdatedChunkNbt")
    public CompletableFuture<Optional<NbtCompound>> c_getUpdatedChunkNbt(ChunkPos chunkPos);

    @Accessor("chunksToUnload")
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> c_getChunksToUnload();
}
