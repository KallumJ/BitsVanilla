package team.bits.vanilla.fabric.pregen;

public record ChunkCoords(int x, int z) {

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChunkCoords chunkCoords) {
            return this.x == chunkCoords.x && this.z == chunkCoords.z;
        }
        return false;
    }
}
