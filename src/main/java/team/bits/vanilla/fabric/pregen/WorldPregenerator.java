package team.bits.vanilla.fabric.pregen;

import net.minecraft.server.world.*;
import org.apache.commons.lang3.time.*;
import org.apache.logging.log4j.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class WorldPregenerator implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private final AtomicLong generatedChunks = new AtomicLong();

    private final ServerWorld world;
    private final ChunkIterator chunkIterator;

    private long lastStatusTime;
    private long lastGeneratedChunks;

    private boolean stopped;

    public WorldPregenerator(ServerWorld world, ChunkIterator chunkIterator) {
        this.world = world;
        this.chunkIterator = chunkIterator;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        long startTime = System.currentTimeMillis();

        // a semaphore to limit us to generating 50 chunks at once
        Semaphore concurrentChunkGenSemaphore = new Semaphore(50);

        while (!this.stopped && this.chunkIterator.hasNext()) {
            ChunkCoords chunk = this.chunkIterator.next();

            try {
                concurrentChunkGenSemaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            WorldPregenUtils.generateChunkAsync(this.world, chunk.x(), chunk.z()).whenComplete((_void, _error) -> {
                concurrentChunkGenSemaphore.release();
                this.generatedChunks.incrementAndGet();
            });

            this.printTimings();
        }
        long taken = System.currentTimeMillis() - startTime;
        LOGGER.info("World generation finished, took {}", DurationFormatUtils.formatDurationHMS(taken));
    }

    public void stop() {
        this.stopped = true;
    }

    private void printTimings() {
        // print timings every 5 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastStatusTime > (5 * 1000)) {
            this.lastStatusTime = currentTime;

            long newGeneratedChunks = this.generatedChunks.get() - this.lastGeneratedChunks;
            int chunksPerSecond = Math.round((float) newGeneratedChunks / 5f);
            int totalGeneratedPercent = (int) (((float) this.generatedChunks.get() / this.chunkIterator.size()) * 100f);
            LOGGER.info("Total generated: {}/{} ({}%) @ {} chunks/sec",
                    this.generatedChunks.get(), this.chunkIterator.size(), totalGeneratedPercent, chunksPerSecond);

            this.lastGeneratedChunks = this.generatedChunks.get();
        }
    }
}
