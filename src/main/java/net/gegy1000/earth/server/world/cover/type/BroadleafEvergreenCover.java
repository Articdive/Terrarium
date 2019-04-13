package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.ClimaticZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.awt.Color;
import java.util.Random;

public class BroadleafEvergreenCover extends ForestCover {
    public BroadleafEvergreenCover() {
        super(new Color(0x006300));
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Decoration extends ForestCover.Decoration {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
            World world = this.context.getWorld();
            ClimaticZone zone = this.context.getZone(chunkPos);

            this.preventIntersection(zone == ClimaticZone.TROPICS ? 1 : 2);

            int[] clearingLayer = this.sampleChunk(this.clearingSelector, chunkPos);
            int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, chunkPos);

            int oakCount = this.getOakCount(random, zone);
            this.decorateScatter(random, chunkPos, writer, oakCount, (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 4, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(world, random, pos);
                }
            });

            this.decorateScatter(random, chunkPos, writer, this.getBirchCount(random, zone), (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 4, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(world, random, pos);
                }
            });

            int jungleCount = this.getJungleCount(random, zone);
            this.decorateScatter(random, chunkPos, writer, jungleCount, (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 4, 9) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(world, random, pos);
                }
            });

            this.stopIntersectionPrevention();

            this.decorateScatter(random, chunkPos, writer, oakCount + 2, (pos, localX, localZ) -> OAK_DENSE_SHRUB.generate(world, random, pos));
            this.decorateScatter(random, chunkPos, writer, jungleCount + 2, (pos, localX, localZ) -> JUNGLE_DENSE_SHRUB.generate(world, random, pos));
        }

        @Override
        public int getMaxHeightOffset() {
            return 6;
        }

        private int getOakCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case SUBTROPICS:
                    return this.range(random, 1, 5);
                case TROPICS:
                    return this.range(random, 0, 4);
                case FRIGID:
                    return this.range(random, 0, 2);
                default:
                    return this.range(random, 2, 7);
            }
        }

        private int getBirchCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case TEMPERATE:
                    return this.range(random, 1, 5);
                case FRIGID:
                    return this.range(random, 0, 2);
                default:
                    return 0;
            }
        }

        private int getJungleCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case SUBTROPICS:
                    return this.range(random, 1, 4);
                case TROPICS:
                    return this.range(random, 3, 7);
                case FRIGID:
                    return 0;
                default:
                    return this.range(random, -2, 2);
            }
        }
    }
}
