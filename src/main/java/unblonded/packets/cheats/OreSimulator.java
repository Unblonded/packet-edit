package unblonded.packets.cheats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import unblonded.packets.util.AncientDebrisUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class OreSimulator {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final Map<Long, Set<Vec3d>> chunkDebrisPositions = new ConcurrentHashMap<>();

    static AncientDebrisUtil dataSmall = AncientDebrisUtil.createForFeature(OrePlacedFeatures.ORE_DEBRIS_SMALL, 7);
	static AncientDebrisUtil dataLarge = AncientDebrisUtil.createForFeature(OrePlacedFeatures.ORE_ANCIENT_DEBRIS_LARGE, 7);

	private static long worldSeed;
	private static int horizontalRadius = 5;

	public static void setHorizontalRadius(int radius) {
		horizontalRadius = radius;
	}

	public static void setWorldSeed(long seed) {
		worldSeed = seed;
	}


	public static void recalculateChunks() {
		if (mc.player == null || mc.world == null) return;
		chunkDebrisPositions.clear();

		ChunkPos playerChunkPos = mc.player.getChunkPos();
		int chunkX = playerChunkPos.x;
		int chunkZ = playerChunkPos.z;

		for (int x = chunkX - horizontalRadius; x <= chunkX + horizontalRadius; x++) {
			for (int z = chunkZ - horizontalRadius; z <= chunkZ + horizontalRadius; z++) {
				Chunk chunk = mc.world.getChunk(x, z, ChunkStatus.BIOMES, false);
				if (chunk != null) {
					doMathOnChunk(chunk);
				}
			}
		}
	}

	private static void doMathOnChunk(Chunk chunk) {
		var chunkPos = chunk.getPos();
		ClientWorld world = mc.world;
		long chunkKey = chunkPos.toLong();

		if (chunkDebrisPositions.containsKey(chunkKey) || world == null) return;

		Set<RegistryKey<Biome>> biomes = new HashSet<>();
		ChunkPos.stream(chunkPos, 1).forEach(neighborChunkPos -> {
			Chunk neighborChunk = world.getChunk(neighborChunkPos.x, neighborChunkPos.z, ChunkStatus.BIOMES, false);
			if (neighborChunk == null) return;

			for(ChunkSection chunkSection : neighborChunk.getSectionArray()) {
				if (chunkSection != null && !chunkSection.isEmpty()) {
					chunkSection.getBiomeContainer().forEachValue(entry -> {
						if (entry.getKey().isPresent()) {
							biomes.add(entry.getKey().get());
						}
					});
				}
			}
		});

		boolean hasValidBiome = biomes.stream().anyMatch(OreSimulator::isValidAncientDebrisBiome);
		if (!hasValidBiome) return;

		int chunkX = chunkPos.x << 4;
		int chunkZ = chunkPos.z << 4;

		ChunkRandom random = new ChunkRandom(ChunkRandom.RandomProvider.XOROSHIRO.create(0));
		long populationSeed = random.setPopulationSeed(worldSeed, chunkX, chunkZ);

		Set<Vec3d> debrisPositions = new HashSet<>();

		processDebrisGeneration(world, random, populationSeed, dataSmall, chunkX, chunkZ, debrisPositions);
		processDebrisGeneration(world, random, populationSeed, dataLarge, chunkX, chunkZ, debrisPositions);

		if (!debrisPositions.isEmpty()) {
			chunkDebrisPositions.put(chunkKey, debrisPositions);
		}
	}

	private static void processDebrisGeneration(ClientWorld world, ChunkRandom random, long populationSeed,
                                                AncientDebrisUtil debrisData, int chunkX, int chunkZ,
                                                Set<Vec3d> results) {
		random.setDecoratorSeed(populationSeed, debrisData.index, debrisData.step);
		int count = debrisData.count.get(random);

		for (int i = 0; i < count; i++) {
			if (debrisData.rarity != 1F && random.nextFloat() >= 1/debrisData.rarity) continue;

			int x = random.nextInt(16) + chunkX;
			int z = random.nextInt(16) + chunkZ;
			int y = debrisData.heightProvider.get(random, debrisData.heightContext);

			BlockPos pos = new BlockPos(x, y, z);

			var biomeEntry = world.getBiome(pos);
			if (!biomeEntry.getKey().isPresent() || !isValidAncientDebrisBiome(biomeEntry.getKey().get())) continue;

            ArrayList<Vec3d> generatedPositions;
            if (debrisData.scattered)
				generatedPositions = generateHidden(world, random, pos, debrisData.size);
            else
				generatedPositions = generateNormal(world, random, pos, debrisData.size, debrisData.discardOnAirChance);
            results.addAll(generatedPositions);
        }
	}

	private static ArrayList<Vec3d> generateNormal(ClientWorld world, ChunkRandom random, BlockPos blockPos, int veinSize, float discardOnAir) {
		float f = random.nextFloat() * 3.1415927F;
		float g = (float) veinSize / 8.0F;
		int i = MathHelper.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
		double d = (double) blockPos.getX() + Math.sin(f) * (double) g;
		double e = (double) blockPos.getX() - Math.sin(f) * (double) g;
		double h = (double) blockPos.getZ() + Math.cos(f) * (double) g;
		double j = (double) blockPos.getZ() - Math.cos(f) * (double) g;
		double l = (blockPos.getY() + random.nextInt(3) - 2);
		double m = (blockPos.getY() + random.nextInt(3) - 2);
		int n = blockPos.getX() - MathHelper.ceil(g) - i;
		int o = blockPos.getY() - 2 - i;
		int p = blockPos.getZ() - MathHelper.ceil(g) - i;
		int q = 2 * (MathHelper.ceil(g) + i);
		int r = 2 * (2 + i);

		for (int s = n; s <= n + q; ++s) {
			for (int t = p; t <= p + q; ++t) {
				if (o <= world.getTopY(Heightmap.Type.MOTION_BLOCKING, s, t)) {
					return generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir);
				}
			}
		}

		return new ArrayList<>();
	}

	private static ArrayList<Vec3d> generateVeinPart(ClientWorld world, ChunkRandom random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int size, int i, float discardOnAir) {
		BitSet bitSet = new BitSet(size * i * size);
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		double[] ds = new double[veinSize * 4];
		ArrayList<Vec3d> poses = new ArrayList<>();

		int n;
		double p;
		double q;
		double r;
		double s;

		for (n = 0; n < veinSize; ++n) {
			float f = (float) n / (float) veinSize;
			p = MathHelper.lerp(f, startX, endX);
			q = MathHelper.lerp(f, startY, endY);
			r = MathHelper.lerp(f, startZ, endZ);
			s = random.nextDouble() * (double) veinSize / 16.0D;
			double m = ((double) (MathHelper.sin(3.1415927F * f) + 1.0F) * s + 1.0D) / 2.0D;
			ds[n * 4] = p;
			ds[n * 4 + 1] = q;
			ds[n * 4 + 2] = r;
			ds[n * 4 + 3] = m;
		}

		for (n = 0; n < veinSize - 1; ++n) {
			if (!(ds[n * 4 + 3] <= 0.0D)) {
				for (int o = n + 1; o < veinSize; ++o) {
					if (!(ds[o * 4 + 3] <= 0.0D)) {
						p = ds[n * 4] - ds[o * 4];
						q = ds[n * 4 + 1] - ds[o * 4 + 1];
						r = ds[n * 4 + 2] - ds[o * 4 + 2];
						s = ds[n * 4 + 3] - ds[o * 4 + 3];
						if (s * s > p * p + q * q + r * r) {
							if (s > 0.0D) {
								ds[o * 4 + 3] = -1.0D;
							} else {
								ds[n * 4 + 3] = -1.0D;
							}
						}
					}
				}
			}
		}

		for (n = 0; n < veinSize; ++n) {
			double u = ds[n * 4 + 3];
			if (!(u < 0.0D)) {
				double v = ds[n * 4];
				double w = ds[n * 4 + 1];
				double aa = ds[n * 4 + 2];
				int ab = Math.max(MathHelper.floor(v - u), x);
				int ac = Math.max(MathHelper.floor(w - u), y);
				int ad = Math.max(MathHelper.floor(aa - u), z);
				int ae = Math.max(MathHelper.floor(v + u), ab);
				int af = Math.max(MathHelper.floor(w + u), ac);
				int ag = Math.max(MathHelper.floor(aa + u), ad);

				for (int ah = ab; ah <= ae; ++ah) {
					double ai = ((double) ah + 0.5D - v) / u;
					if (ai * ai < 1.0D) {
						for (int aj = ac; aj <= af; ++aj) {
							double ak = ((double) aj + 0.5D - w) / u;
							if (ai * ai + ak * ak < 1.0D) {
								for (int al = ad; al <= ag; ++al) {
									double am = ((double) al + 0.5D - aa) / u;
									if (ai * ai + ak * ak + am * am < 1.0D) {
										int an = ah - x + (aj - y) * size + (al - z) * size * i;
										if (!bitSet.get(an)) {
											bitSet.set(an);
											mutable.set(ah, aj, al);
											if (aj >= -64 && aj < 320) {
												if (shouldPlace(world, mutable, discardOnAir, random)) {
													poses.add(new Vec3d(ah, aj, al));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return poses;
	}

	private static boolean shouldPlace(ClientWorld world, BlockPos orePos, float discardOnAir, ChunkRandom random) {
		if (discardOnAir == 0F) return true;
		if (discardOnAir != 1F && random.nextFloat() >= discardOnAir) return true;

		for (Direction direction : Direction.values()) {
			BlockPos adjacentPos = orePos.add(direction.getVector());

			if (world.isChunkLoaded(adjacentPos)) {
				if (!world.getBlockState(adjacentPos).isOpaque() && discardOnAir != 1F) {
					return false;
				}
			}
		}

		return true;
	}

	private static ArrayList<Vec3d> generateHidden(ClientWorld world, ChunkRandom random, BlockPos blockPos, int size) {
		ArrayList<Vec3d> poses = new ArrayList<>();
		int count = random.nextInt(size + 1);

		for (int j = 0; j < count; ++j) {
			int actualSize = Math.min(j, 7);

			int x = randomCoord(random, actualSize) + blockPos.getX();
			int y = randomCoord(random, actualSize) + blockPos.getY();
			int z = randomCoord(random, actualSize) + blockPos.getZ();

			BlockPos pos = new BlockPos(x, y, z);

			if (world.isChunkLoaded(pos)) {
				if (shouldPlace(world, pos, 1F, random)) {
					poses.add(new Vec3d(x, y, z));
				}
			}
		}

		return poses;
	}

	private static int randomCoord(ChunkRandom random, int size) {
		return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
	}

	public static boolean inNether() {
		if (mc.player == null || mc.world == null) return false;
		return mc.world.getRegistryKey() == ClientWorld.NETHER;
	}

	private static boolean isValidAncientDebrisBiome(RegistryKey<Biome> biome) {
		return biome == BiomeKeys.NETHER_WASTES
				|| biome == BiomeKeys.SOUL_SAND_VALLEY
				|| biome == BiomeKeys.CRIMSON_FOREST
				|| biome == BiomeKeys.WARPED_FOREST
				|| biome == BiomeKeys.BASALT_DELTAS;
	}
}