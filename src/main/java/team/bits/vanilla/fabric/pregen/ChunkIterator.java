package team.bits.vanilla.fabric.pregen;

import java.util.*;

public class ChunkIterator implements Iterator<ChunkCoords> {

    private final int minX;
    private final int maxX;
    private final int maxZ;
    private final int size;

    private int x;
    private int z;

    private boolean hasNext = true;

    public ChunkIterator(int x, int z, int radius) {
        this.minX = x - radius;
        this.maxX = x + radius;
        this.maxZ = z + radius;
        this.size = (radius * 2) * (radius * 2);
        this.x = this.minX;
        this.z = z - radius;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public ChunkCoords next() {
        final ChunkCoords chunkCoord = new ChunkCoords(this.x, this.z);
        if (++this.x > this.maxX) {
            this.x = this.minX;
            if (++this.z > this.maxZ) {
                this.hasNext = false;
            }
        }
        return chunkCoord;
    }

    public int size() {
        return this.size;
    }
}
