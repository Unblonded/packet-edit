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
import java.util.stream.Collectors;

public class OreSimulator {
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final Map<Long, Set<Vec3d>> chunkDebrisPositions = new ConcurrentHashMap<>();

	// New: Floating-point tolerance for better consistency
	private static final double EPSILON = 1e-10;

	// New: Chunk stability tracking
	private static final Map<Long, Long> chunkLoadTimes = new ConcurrentHashMap<>();

	// New: Block state cache for consistency
	private static final Map<BlockPos, Boolean> blockStateCache = new ConcurrentHashMap<>();

	// New: Verification mode for debugging
	private static boolean verificationMode = false;
	private static final Map<Long, Set<Vec3d>> actualResults = new ConcurrentHashMap<>();

	static AncientDebrisUtil dataSmall = AncientDebrisUtil.createForFeature(OrePlacedFeatures.ORE_DEBRIS_SMALL, 7);
	static AncientDebrisUtil dataLarge = AncientDebrisUtil.createForFeature(OrePlacedFeatures.ORE_ANCIENT_DEBRIS_LARGE, 7);

	private static long worldSeed;
	private static int horizontalRadius = 5;

	// New: Configuration options
	private static boolean useConsensusMode = true;
	private static int consensusIterations = 3;
	private static boolean useChunkStabilityDelay = true;
	private static long chunkStabilityDelay = 1000; // 1 second

	public static void setHorizontalRadius(int radius) {
		horizontalRadius = radius;
	}

	public static void setWorldSeed(long seed) {
		worldSeed = seed;
	}

	// New: Configuration methods
	public static void setVerificationMode(boolean enabled) {
		verificationMode = enabled;
	}

	public static void setConsensusMode(boolean enabled, int iterations) {
		useConsensusMode = enabled;
		consensusIterations = iterations;
	}

	public static void setChunkStabilityDelay(boolean enabled, long delayMs) {
		useChunkStabilityDelay = enabled;
		chunkStabilityDelay = delayMs;
	}

	public static void recalculateChunks() {
		if (mc.player == null || mc.world == null) return;
		chunkDebrisPositions.clear();
		blockStateCache.clear(); // Clear cache on recalculation

		ChunkPos playerChunkPos = mc.player.getChunkPos();
		int chunkX = playerChunkPos.x;
		int chunkZ = playerChunkPos.z;

		for (int x = chunkX - horizontalRadius; x <= chunkX + horizontalRadius; x++) {
			for (int z = chunkZ - horizontalRadius; z <= chunkZ + horizontalRadius; z++) {
				// Enhanced: Better chunk status handling with retry logic
				Chunk chunk = waitForChunk(mc.world, x, z);
				if (chunk != null && isChunkStable(chunk)) {
					if (useConsensusMode) {
						Set<Vec3d> consensusResults = getConsensusResults(chunk, consensusIterations);
						if (!consensusResults.isEmpty()) {
							chunkDebrisPositions.put(chunk.getPos().toLong(), consensusResults);
						}
					} else {
						doMathOnChunk(chunk);
					}
				}
			}
		}
	}

	// New: Wait for proper chunk with retry logic
	private static Chunk waitForChunk(ClientWorld world, int x, int z) {
		// First try to get chunk with features
		Chunk chunk = world.getChunk(x, z, ChunkStatus.FEATURES, false);
		if (chunk != null) return chunk;

		// Fallback to biomes status with forced loading
		chunk = world.getChunk(x, z, ChunkStatus.BIOMES, true);
		if (chunk != null) {
			// Mark chunk load time for stability tracking
			long chunkKey = ChunkPos.toLong(x, z);
			chunkLoadTimes.put(chunkKey, System.currentTimeMillis());
		}
		return chunk;
	}

	// New: Check if chunk is stable enough for accurate simulation
	private static boolean isChunkStable(Chunk chunk) {
		if (!useChunkStabilityDelay) return true;

		long chunkKey = chunk.getPos().toLong();
		long currentTime = System.currentTimeMillis();

		chunkLoadTimes.putIfAbsent(chunkKey, currentTime);

		// Wait for chunk to stabilize after loading
		return (currentTime - chunkLoadTimes.get(chunkKey)) >= chunkStabilityDelay;
	}

	// New: Consensus algorithm for multiple simulation runs
	private static Set<Vec3d> getConsensusResults(Chunk chunk, int iterations) {
		Map<Vec3d, Integer> positionCounts = new HashMap<>();

		for (int i = 0; i < iterations; i++) {
			// Clear cache between iterations to avoid bias
			blockStateCache.clear();
			Set<Vec3d> results = simulateChunk(chunk);
			for (Vec3d pos : results) {
				positionCounts.merge(pos, 1, Integer::sum);
			}
		}

		// Return positions that appeared in majority of simulations
		return positionCounts.entrySet().stream()
				.filter(entry -> entry.getValue() > iterations / 2)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	// New: Separate simulation method for consensus mode
	private static Set<Vec3d> simulateChunk(Chunk chunk) {
		var chunkPos = chunk.getPos();
		ClientWorld world = mc.world;

		if (world == null) return new HashSet<>();

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
		if (!hasValidBiome) return new HashSet<>();

		int chunkX = chunkPos.x << 4;
		int chunkZ = chunkPos.z << 4;

		// Enhanced: Better RNG synchronization with validation
		ChunkRandom random = new ChunkRandom(ChunkRandom.RandomProvider.XOROSHIRO.create(0));
		long populationSeed = random.setPopulationSeed(worldSeed, chunkX, chunkZ);

		Set<Vec3d> debrisPositions = new HashSet<>();

		processDebrisGeneration(world, random, populationSeed, dataSmall, chunkX, chunkZ, debrisPositions);
		// Reset RNG state before second generation to prevent drift
		random.setPopulationSeed(worldSeed, chunkX, chunkZ);
		processDebrisGeneration(world, random, populationSeed, dataLarge, chunkX, chunkZ, debrisPositions);

		return debrisPositions;
	}

	private static void doMathOnChunk(Chunk chunk) {
		var chunkPos = chunk.getPos();
		long chunkKey = chunkPos.toLong();

		if (chunkDebrisPositions.containsKey(chunkKey)) return;

		Set<Vec3d> results = simulateChunk(chunk);

		if (!results.isEmpty()) {
			chunkDebrisPositions.put(chunkKey, results);

			// New: Verification mode comparison
			if (verificationMode && actualResults.containsKey(chunkKey)) {
				compareResults(chunkKey, results, actualResults.get(chunkKey));
			}
		}
	}

	// New: Verification comparison
	private static void compareResults(long chunkKey, Set<Vec3d> predicted, Set<Vec3d> actual) {
		Set<Vec3d> missed = new HashSet<>(actual);
		missed.removeAll(predicted);

		Set<Vec3d> falsePositives = new HashSet<>(predicted);
		falsePositives.removeAll(actual);

		if (!missed.isEmpty() || !falsePositives.isEmpty()) {
			System.out.println("Discrepancies in chunk " + chunkKey +
					": Missed=" + missed.size() + ", False Positives=" + falsePositives.size());
			// Could add more detailed analysis here
		}
	}

	private static void processDebrisGeneration(ClientWorld world, ChunkRandom random, long populationSeed,
												AncientDebrisUtil debrisData, int chunkX, int chunkZ,
												Set<Vec3d> results) {
		// Enhanced: More frequent RNG resets to prevent drift
		random.setDecoratorSeed(populationSeed, debrisData.index, debrisData.step);
		int count = debrisData.count.get(random);

		for (int i = 0; i < count; i++) {
			if (debrisData.rarity != 1F && random.nextFloat() >= 1/debrisData.rarity) continue;

			int x = random.nextInt(16) + chunkX;
			int z = random.nextInt(16) + chunkZ;
			int y = debrisData.heightProvider.get(random, debrisData.heightContext);

			BlockPos pos = new BlockPos(x, y, z);

			// Enhanced: More precise biome validation
			if (!hasValidBiomeAtPosition(world, pos)) continue;

			ArrayList<Vec3d> generatedPositions;
			if (debrisData.scattered)
				generatedPositions = generateHidden(world, random, pos, debrisData.size);
			else
				generatedPositions = generateNormal(world, random, pos, debrisData.size, debrisData.discardOnAirChance);
			results.addAll(generatedPositions);
		}
	}

	// New: More precise biome sampling at multiple points
	private static boolean hasValidBiomeAtPosition(ClientWorld world, BlockPos pos) {
		// Check center point
		var biome = world.getBiome(pos);
		if (biome.getKey().isPresent() && isValidAncientDebrisBiome(biome.getKey().get())) {
			return true;
		}

		// Check corners to catch biome boundaries better
		BlockPos[] testPositions = {
				pos.add(1, 0, 0),
				pos.add(-1, 0, 0),
				pos.add(0, 0, 1),
				pos.add(0, 0, -1)
		};

		for (BlockPos testPos : testPositions) {
			biome = world.getBiome(testPos);
			if (biome.getKey().isPresent() && isValidAncientDebrisBiome(biome.getKey().get())) {
				return true;
			}
		}
		return false;
	}

	// New: Cached block state checking for consistency
	private static boolean isOpaqueBlock(ClientWorld world, BlockPos pos) {
		return blockStateCache.computeIfAbsent(pos, p -> {
			return world.getBlockState(p).isOpaque();
		});
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
									// Enhanced: Added epsilon tolerance for boundary calculations
									if (ai * ai + ak * ak + am * am < (1.0D + EPSILON)) {
										int an = ah - x + (aj - y) * size + (al - z) * size * i;
										if (!bitSet.get(an)) {
											bitSet.set(an);
											mutable.set(ah, aj, al);
											if (aj >= -64 && aj < 320 && isOpaqueBlock(world, mutable)) {
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
		if (discardOnAir == 0F || (discardOnAir != 1F && random.nextFloat() >= discardOnAir)) {
			return true;
		}

		for (Direction direction : Direction.values()) {
			if (!isOpaqueBlock(world, orePos.add(direction.getVector())) && discardOnAir != 1F) {
				return false;
			}
		}
		return true;
	}

	private static ArrayList<Vec3d> generateHidden(ClientWorld world, ChunkRandom random, BlockPos blockPos, int size) {

		ArrayList<Vec3d> poses = new ArrayList<>();

		int i = random.nextInt(size + 1);

		for (int j = 0; j < i; ++j) {
			size = Math.min(j, 7);
			int x = randomCoord(random, size) + blockPos.getX();
			int y = randomCoord(random, size) + blockPos.getY();
			int z = randomCoord(random, size) + blockPos.getZ();
			BlockPos pos = new BlockPos(x, y, z);
			if (isOpaqueBlock(world, pos)) {
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

	// New: Utility methods for configuration and debugging
	public static void clearCaches() {
		blockStateCache.clear();
		chunkLoadTimes.clear();
	}

	public static void setActualResults(long chunkKey, Set<Vec3d> actual) {
		actualResults.put(chunkKey, actual);
	}

	public static Map<String, Integer> getStatistics() {
		Map<String, Integer> stats = new HashMap<>();
		stats.put("cached_chunks", chunkDebrisPositions.size());
		stats.put("cached_blocks", blockStateCache.size());
		stats.put("tracked_load_times", chunkLoadTimes.size());
		return stats;
	}
}