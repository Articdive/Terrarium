package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.api.CubicChunkPrimer;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.primer.CubicCavePrimer;
import net.gegy1000.cubicglue.util.primer.CubicRavinePrimer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.minecraft.world.World;

public class CaveSurfaceComposer implements SurfaceComposer {
    private final CubicChunkPrimer caveGenerator;
    private final CubicChunkPrimer ravineGenerator;

    public CaveSurfaceComposer(World world) {
        this.caveGenerator = new CubicCavePrimer(world);
        this.ravineGenerator = new CubicRavinePrimer(world, world.getSeaLevel());
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        this.caveGenerator.prime(pos, writer);
        this.ravineGenerator.prime(pos, writer);
    }
}
