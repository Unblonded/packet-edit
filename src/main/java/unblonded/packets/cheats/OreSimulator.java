package unblonded.packets.cheats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import unblonded.packets.util.Color;
import unblonded.packets.util.OreUtil;
import unblonded.packets.util.PosColor;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class OreSimulator {
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final Map<Long, Set<PosColor>> chunkDebrisPositions = new ConcurrentHashMap<>();

	private static final double EPSILON = 1e-10;
	private static final Map<Long, Long> chunkLoadTimes = new ConcurrentHashMap<>();
	private static final Map<BlockPos, Boolean> blockStateCache = new ConcurrentHashMap<>();
	private static boolean verificationMode = false;
	private static final Map<Long, Set<PosColor>> actualResults = new ConcurrentHashMap<>();

	public static Map<RegistryKey<Biome>, List<OreUtil>> ores;
	public static long worldSeed;
	public static int horizontalRadius = 5;

	private static boolean useConsensusMode = true;
	private static int consensusIterations = 3;
	private static boolean useChunkStabilityDelay = true;
	private static long chunkStabilityDelay = 1000;

	public static void recalculateChunksAsync() {
		if (mc.player == null || mc.world == null) return;

		CompletableFuture.runAsync(() -> {
			executor.execute(() -> {
				Map<Long, Set<PosColor>> newChunkDebrisPositions = new ConcurrentHashMap<>();

				ChunkPos playerChunkPos = mc.player.getChunkPos();
				int chunkX = playerChunkPos.x;
				int chunkZ = playerChunkPos.z;

				for (int x = chunkX - horizontalRadius; x <= chunkX + horizontalRadius; x++) {
					for (int z = chunkZ - horizontalRadius; z <= chunkZ + horizontalRadius; z++) {

						Chunk chunk = waitForChunk(mc.world, x, z);
						if (chunk != null && isChunkStable(chunk)) {
							if (useConsensusMode) {
								Set<PosColor> consensusResults = getConsensusResults(chunk, consensusIterations);
								if (!consensusResults.isEmpty()) {
									newChunkDebrisPositions.put(chunk.getPos().toLong(), consensusResults);
								}
							} else {
								doMathOnChunk(chunk, newChunkDebrisPositions);
							}
						}
					}
				}

				executor.execute(() -> {
					OreSimulator.ores = OreUtil.getRegistry(mc.world.getRegistryKey());
					chunkDebrisPositions.clear();
					chunkDebrisPositions.putAll(newChunkDebrisPositions);
					blockStateCache.clear();
				});
			});
		});
	}

	private static Chunk waitForChunk(ClientWorld world, int x, int z) {
		Chunk chunk = world.getChunk(x, z, ChunkStatus.FEATURES, false);
		if (chunk != null) return chunk;

		chunk = world.getChunk(x, z, ChunkStatus.BIOMES, true);
		if (chunk != null) {
			long chunkKey = ChunkPos.toLong(x, z);
			chunkLoadTimes.put(chunkKey, System.currentTimeMillis());
		}
		return chunk;
	}

	private static boolean isChunkStable(Chunk chunk) {
		if (!useChunkStabilityDelay) return true;

		long chunkKey = chunk.getPos().toLong();
		long currentTime = System.currentTimeMillis();

		chunkLoadTimes.putIfAbsent(chunkKey, currentTime);

		return (currentTime - chunkLoadTimes.get(chunkKey)) >= chunkStabilityDelay;
	}

	private static Set<PosColor> getConsensusResults(Chunk chunk, int iterations) {
		Map<PosColor, Integer> positionCounts = new HashMap<>();

		for (int i = 0; i < iterations; i++) {
			blockStateCache.clear();
			Set<PosColor> results = simulateChunk(chunk);
			for (PosColor pos : results) {
				positionCounts.merge(pos, 1, Integer::sum);
			}
		}

		return positionCounts.entrySet().stream()
				.filter(entry -> entry.getValue() > iterations / 2)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	private static Set<PosColor> simulateChunk(Chunk chunk) {
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

		int chunkX = chunkPos.x << 4;
		int chunkZ = chunkPos.z << 4;

		ChunkRandom random = new ChunkRandom(ChunkRandom.RandomProvider.XOROSHIRO.create(0));
		long populationSeed = random.setPopulationSeed(worldSeed, chunkX, chunkZ);

		Set<PosColor> debrisPositions = new HashSet<>();

		random.setPopulationSeed(worldSeed, chunkX, chunkZ);

		for (List<OreUtil> oreList : ores.values())
			for (OreUtil oreData : oreList)
				processDebrisGeneration(world, random, populationSeed, oreData, chunkX, chunkZ, debrisPositions);

		return debrisPositions;
	}

	private static void doMathOnChunk(Chunk chunk, Map<Long, Set<PosColor>> outputMap) {
		var chunkPos = chunk.getPos();
		long chunkKey = chunkPos.toLong();

		if (chunkDebrisPositions.containsKey(chunkKey) || outputMap.containsKey(chunkKey)) return;

		Set<PosColor> results = simulateChunk(chunk);

		if (!results.isEmpty()) {
			outputMap.put(chunkKey, results);
			if (verificationMode && actualResults.containsKey(chunkKey)) {
				compareResults(chunkKey, results, actualResults.get(chunkKey));
			}
		}
	}

	private static void compareResults(long chunkKey, Set<PosColor> predicted, Set<PosColor> actual) {
		Set<PosColor> missed = new HashSet<>(actual);
		missed.removeAll(predicted);

		Set<PosColor> falsePositives = new HashSet<>(predicted);
		falsePositives.removeAll(actual);

		if (!missed.isEmpty() || !falsePositives.isEmpty()) {
			System.out.println("Errors in chunk " + chunkKey +
					": Missed=" + missed.size() + ", False Positives=" + falsePositives.size());
		}
	}

	private static void processDebrisGeneration(ClientWorld world, ChunkRandom random, long populationSeed,
												OreUtil data, int chunkX, int chunkZ,
												Set<PosColor> results) {

		random.setDecoratorSeed(populationSeed, data.index, data.step);
		int count = data.count.get(random);

		for (int i = 0; i < count; i++) {
			if (data.rarity != 1F && random.nextFloat() >= 1/data.rarity) continue;

			int x = random.nextInt(16) + chunkX;
			int z = random.nextInt(16) + chunkZ;
			int y = data.heightProvider.get(random, data.heightContext);

			PosColor pos = new PosColor(new BlockPos(x, y, z), data.color);

			ArrayList<PosColor> generatedPositions;
			if (data.scattered)
				generatedPositions = generateHidden(world, random, pos, data.size);
			else
				generatedPositions = generateNormal(world, random, pos, data.size, data.discardOnAirChance);
			results.addAll(generatedPositions);
		}
	}

	private static boolean isOpaqueBlock(ClientWorld world, BlockPos pos) {
		return blockStateCache.computeIfAbsent(pos, p -> world.getBlockState(p).isOpaque());
	}

	private static ArrayList<PosColor> generateNormal(ClientWorld world, ChunkRandom random, PosColor blockPos, int veinSize, float discardOnAir) {
		float f = random.nextFloat() * 3.1415927F;
		float g = (float) veinSize / 8.0F;
		int i = MathHelper.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
		double d = (double) blockPos.pos.getX() + Math.sin(f) * (double) g;
		double e = (double) blockPos.pos.getX() - Math.sin(f) * (double) g;
		double h = (double) blockPos.pos.getZ() + Math.cos(f) * (double) g;
		double j = (double) blockPos.pos.getZ() - Math.cos(f) * (double) g;
		double l = (blockPos.pos.getY() + random.nextInt(3) - 2);
		double m = (blockPos.pos.getY() + random.nextInt(3) - 2);
		int n = blockPos.pos.getX() - MathHelper.ceil(g) - i;
		int o = blockPos.pos.getY() - 2 - i;
		int p = blockPos.pos.getZ() - MathHelper.ceil(g) - i;
		int q = 2 * (MathHelper.ceil(g) + i);
		int r = 2 * (2 + i);

		for (int s = n; s <= n + q; ++s) {
			for (int t = p; t <= p + q; ++t) {
				if (o <= world.getTopY(Heightmap.Type.MOTION_BLOCKING, s, t)) {
					return generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir, blockPos.color);
				}
			}
		}

		return new ArrayList<>();
	}

	private static ArrayList<PosColor> generateVeinPart(ClientWorld world, ChunkRandom random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int size, int i, float discardOnAir, Color color) {
		BitSet bitSet = new BitSet(size * i * size);
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		double[] ds = new double[veinSize * 4];
		ArrayList<PosColor> poses = new ArrayList<>();

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
									if (ai * ai + ak * ak + am * am < (1.0D + EPSILON)) {
										int an = ah - x + (aj - y) * size + (al - z) * size * i;
										if (!bitSet.get(an)) {
											bitSet.set(an);
											mutable.set(ah, aj, al);
											if (aj >= -64 && aj < 320 && isOpaqueBlock(world, mutable)) {
												if (shouldPlace(world, mutable, discardOnAir, random)) {
													poses.add(new PosColor(new BlockPos(ah, aj, al), color));
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
		if (discardOnAir == 0F || (discardOnAir != 1F && random.nextFloat() >= discardOnAir)) return true;

		for (Direction direction : Direction.values())
			if (!isOpaqueBlock(world, orePos.add(direction.getVector())) && discardOnAir != 1F)
				return false;
		return true;
	}

	private static ArrayList<PosColor> generateHidden(ClientWorld world, ChunkRandom random, PosColor blockPos, int size) {
		ArrayList<PosColor> poses = new ArrayList<>();
		int i = random.nextInt(size + 1);

		for (int j = 0; j < i; ++j) {
			size = Math.min(j, 7);
			int x = randomCoord(random, size) + blockPos.pos.getX();
			int y = randomCoord(random, size) + blockPos.pos.getY();
			int z = randomCoord(random, size) + blockPos.pos.getZ();
			BlockPos pos = new BlockPos(x, y, z);
			if (isOpaqueBlock(world, pos)) {
				if (shouldPlace(world, pos, 1F, random)) {
					poses.add(new PosColor(new BlockPos(x, y, z), blockPos.color));
				}
			}
		}

		return poses;
	}

	private static int randomCoord(ChunkRandom random, int size) {
		return Math.round((random.nextFloat() - random.nextFloat()) * (float) size);
	}
}