package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

import java.util.Random;

public class HeightNoiseAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;

    private final NoiseGeneratorOctaves heightNoise;
    private final double noiseMax;
    private final double noiseScaleXZ;
    private final double noiseScaleY;

    public HeightNoiseAdapter(World world, RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<WaterRasterTile> waterComponent, int octaveCount, double noiseScaleXZ, double noiseScaleY) {
        this.heightComponent = heightComponent;
        this.waterComponent = waterComponent;

        this.heightNoise = new NoiseGeneratorOctaves(new Random(world.getWorldInfo().getSeed()), octaveCount);

        double max = 0.0;
        double scale = 1.0;
        for (int i = 0; i < octaveCount; i++) {
            max += scale * 2;
            scale /= 2.0;
        }
        this.noiseMax = max;

        this.noiseScaleXZ = noiseScaleXZ;
        this.noiseScaleY = noiseScaleY;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        if (this.noiseScaleY > -1e-3 && this.noiseScaleY < 1e-3) {
            return;
        }

        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);

        short[] heightBuffer = heightTile.getShortData();

        double[] noise = new double[width * height];
        this.heightNoise.generateNoiseOctaves(noise, z, x, width, height, this.noiseScaleXZ, this.noiseScaleXZ, 0.0);

        short[] waterBuffer = waterTile.getShortData();
        for (int i = 0; i < noise.length; i++) {
            if (WaterRasterTile.isLand(waterBuffer[i])) {
                heightBuffer[i] += (noise[i] + this.noiseMax) / (this.noiseMax * 2.0) * this.noiseScaleY * 35.0;
            }
        }
    }
}
