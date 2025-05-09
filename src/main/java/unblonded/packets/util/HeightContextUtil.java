package unblonded.packets.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HeightContextUtil {
    public static HeightContext createCustomHeightContext(int bottomY, int logicalHeight) {
        ChunkGenerator dummyGenerator = new DummyChunkGenerator(bottomY, bottomY + logicalHeight);
        HeightLimitView heightLimitView = HeightLimitView.create(bottomY, logicalHeight);
        return new HeightContext(dummyGenerator, heightLimitView);
    }

    private static class DummyChunkGenerator extends ChunkGenerator {
        public DummyChunkGenerator(int minY, int worldHeight) {super(null);}

        @Override public int getMinimumY() {return -9999999;}
        @Override public int getWorldHeight() {return 100000000;}

        @Override protected MapCodec<? extends ChunkGenerator> getCodec() {return null;}
        @Override public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {}
        @Override public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {}
        @Override public void populateEntities(ChunkRegion region) {}
        @Override public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {return null;}
        @Override public int getSeaLevel() {return 0;}
        @Override public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {return 0;}
        @Override public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {return null;}
        @Override public void appendDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {}
    }
}