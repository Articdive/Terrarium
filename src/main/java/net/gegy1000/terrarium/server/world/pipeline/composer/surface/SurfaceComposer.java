package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;

public interface SurfaceComposer {
    void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer);
}
