package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.region.RegionData;

public class WaterCarveAdapter implements RegionAdapter {
    private static final int SMOOTH_RANGE = 5;

    private final RegionComponentType<WaterRaster> waterComponent;
    private final RegionComponentType<ShortRaster> heightComponent;
    private final int oceanDepth;

    public WaterCarveAdapter(RegionComponentType<WaterRaster> waterComponent, RegionComponentType<ShortRaster> heightComponent, int oceanDepth) {
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.oceanDepth = oceanDepth;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        WaterRaster waterTile = data.getOrExcept(this.waterComponent);
        ShortRaster heightTile = data.getOrExcept(this.heightComponent);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int waterType = waterTile.getWaterType(localX, localZ);
                if (waterType != WaterRaster.LAND) {
                    int waterLevel = waterTile.getWaterLevel(localX, localZ);
                    int depth = waterType == WaterRaster.OCEAN ? this.oceanDepth : Math.min(this.oceanDepth, 4);
                    double depthScale = this.computeDepthScale(waterTile, width, height, localZ, localX);

                    double carvedHeight = waterLevel - depth * depthScale;
                    heightTile.setShort(localX, localZ, (short) Math.round(carvedHeight));
                }
            }
        }
    }

    private double computeDepthScale(WaterRaster waterTile, int width, int height, int x, int z) {
        int landDistance = this.computeLandDistance(waterTile, width, height, z, x);
        return (double) landDistance / SMOOTH_RANGE;
    }

    private int computeLandDistance(WaterRaster waterTile, int width, int height, int x, int z) {
        for (int range = 0; range <= SMOOTH_RANGE; range++) {
            int minX = x - range;
            int maxX = x + range;
            int minZ = z - range;
            int maxZ = z + range;

            for (int nz = minZ; nz <= maxZ; nz++) {
                for (int nx = minX; nx <= maxX; nx++) {
                    if (nx < 0 || nz < 0 || nx >= width || nz >= height) {
                        continue;
                    }
                    if (nx == minX || nz == minZ || nx == maxX || nz == maxZ) {
                        if (waterTile.getWaterType(nx, nz) == WaterRaster.LAND) {
                            return range;
                        }
                    }
                }
            }
        }

        return SMOOTH_RANGE;
    }
}
