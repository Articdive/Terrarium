package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;

public class OceanFillSurfaceComposer implements SurfaceComposer {
    private final DataKey<ShortRaster> heightKey;
    private final IBlockState block;
    private final int oceanLevel;

    public OceanFillSurfaceComposer(DataKey<ShortRaster> heightKey, IBlockState block, int oceanLevel) {
        this.heightKey = heightKey;
        this.block = block;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        data.get(this.heightKey).ifPresent(heightRaster -> {
            int minY = pos.getMinY();
            int maxY = pos.getMaxY();

            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    int height = heightRaster.get(localX, localZ);
                    if (height <= maxY && height < this.oceanLevel) {
                        int minOceanY = Math.max(height + 1, minY);
                        int maxOceanY = Math.min(this.oceanLevel, maxY);
                        for (int localY = minOceanY; localY <= maxOceanY; localY++) {
                            writer.set(localX, localY, localZ, this.block);
                        }
                    }
                }
            }
        });
    }
}
