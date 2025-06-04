package unblonded.packets.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import unblonded.packets.cfg;
import unblonded.packets.mixin.CPMAccess;
import unblonded.packets.mixin.HRPMAccess;
import unblonded.packets.mixin.RFPMAccess;

import java.util.*;
import java.util.List;

public class OreUtil {

    public static final String[] oreNames = new String[] {
            "Coal",
            "Iron",
            "Gold",
            "Redstone",
            "Diamond",
            "Lapis",
            "Copper",
            "Emerald",
            "Quartz",
            "Ancient Debris"
    };

    public static Map<RegistryKey<Biome>, List<OreUtil>> getRegistry(RegistryKey<World> dimension) {

        RegistryWrapper.WrapperLookup registry = BuiltinRegistries.createWrapperLookup();
        RegistryWrapper.Impl<PlacedFeature> features = registry.getOrThrow(RegistryKeys.PLACED_FEATURE);
        var reg = registry.getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.DEFAULT).value().createDimensionsRegistryHolder().dimensions();;

        DimensionOptions dim;

        if (dimension.equals(World.OVERWORLD)) dim = reg.get(DimensionOptions.OVERWORLD);
        else if (dimension.equals(World.NETHER)) dim = reg.get(DimensionOptions.NETHER);
        else if (dimension.equals(World.END)) dim = reg.get(DimensionOptions.END);
        else throw new IllegalStateException("Unexpected dimension: " + dimension);

        var biomes = dim.chunkGenerator().getBiomeSource().getBiomes();
        var biomes1 = biomes.stream().toList();

        List<PlacedFeatureIndexer.IndexedFeatures> indexer = PlacedFeatureIndexer.collectIndexedFeatures(
                biomes1, biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(), true
        );


        Map<PlacedFeature, OreUtil> featureToOre = new HashMap<>();
        if (cfg.oreSimOptions[0].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COAL_LOWER, 6, cfg.oreColors[0]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COAL_UPPER, 6, cfg.oreColors[0]);
        }

        // Iron - Overworld only
        if (cfg.oreSimOptions[1].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_IRON_MIDDLE, 6, cfg.oreColors[1]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_IRON_SMALL, 6, cfg.oreColors[1]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_IRON_UPPER, 6, cfg.oreColors[1]);
        }

        // Gold - Overworld and Nether
        if (cfg.oreSimOptions[2].get()) {
            if (dimension.equals(World.OVERWORLD)) {
                registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD, 6, cfg.oreColors[2]);
                registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_LOWER, 6, cfg.oreColors[2]);
                registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_EXTRA, 6, cfg.oreColors[2]);
            }
            if (dimension.equals(World.NETHER)) {
                registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_NETHER, 7, cfg.oreColors[2]);
                registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_GOLD_DELTAS, 7, cfg.oreColors[2]);
            }
        }

        // Redstone - Overworld only
        if (cfg.oreSimOptions[3].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_REDSTONE, 6, cfg.oreColors[3]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_REDSTONE_LOWER, 6, cfg.oreColors[3]);
        }

        // Diamond - Overworld only
        if (cfg.oreSimOptions[4].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND, 6, cfg.oreColors[4]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND_BURIED, 6, cfg.oreColors[4]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND_LARGE, 6, cfg.oreColors[4]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DIAMOND_MEDIUM, 6, cfg.oreColors[4]);
        }

        // Lapis - Overworld only
        if (cfg.oreSimOptions[5].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_LAPIS, 6, cfg.oreColors[5]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_LAPIS_BURIED, 6, cfg.oreColors[5]);
        }

        // Copper - Overworld only
        if (cfg.oreSimOptions[6].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COPPER, 6, cfg.oreColors[6]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_COPPER_LARGE, 6, cfg.oreColors[6]);
        }

        // Emerald - Overworld only
        if (cfg.oreSimOptions[7].get() && dimension.equals(World.OVERWORLD)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_EMERALD, 6, cfg.oreColors[7]);
        }

        // Quartz - Nether only
        if (cfg.oreSimOptions[8].get() && dimension.equals(World.NETHER)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_QUARTZ_NETHER, 7, cfg.oreColors[8]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_QUARTZ_DELTAS, 7, cfg.oreColors[8]);
        }

        // Ancient Debris - Nether only
        if (cfg.oreSimOptions[9].get() && dimension.equals(World.NETHER)) {
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_DEBRIS_SMALL, 7, cfg.oreColors[9]);
            registerOre(featureToOre, indexer, features, OrePlacedFeatures.ORE_ANCIENT_DEBRIS_LARGE, 7, cfg.oreColors[9]);
        }

        Map<RegistryKey<Biome>, List<OreUtil>> biomeOreMap = new HashMap<>();

        biomes1.forEach(biome -> {
            biomeOreMap.put(biome.getKey().get(), new ArrayList<>());
            biome.value().getGenerationSettings().getFeatures().stream()
                    .flatMap(RegistryEntryList::stream)
                    .map(RegistryEntry::value)
                    .filter(featureToOre::containsKey)
                    .forEach(feature -> {
                        biomeOreMap.get(biome.getKey().get()).add(featureToOre.get(feature));
                    });
        });
        return biomeOreMap;
    }

    private static void registerOre(
            Map<PlacedFeature, OreUtil> map,
            List<PlacedFeatureIndexer.IndexedFeatures> indexer,
            RegistryWrapper.Impl<PlacedFeature> oreRegistry,
            RegistryKey<PlacedFeature> oreKey,
            int genStep,
            Color color
    ) {
        var orePlacement = oreRegistry.getOrThrow(oreKey).value();

        int index = indexer.get(genStep).indexMapping().applyAsInt(orePlacement);

        OreUtil ore = new OreUtil(orePlacement, genStep, index, color, oreKey);

        map.put(orePlacement, ore);
    }

    // Added registry key field
    public RegistryKey<PlacedFeature> registryKey;
    public int step;
    public int index;
    public IntProvider count = ConstantIntProvider.create(1);
    public HeightProvider heightProvider;
    public HeightContext heightContext;
    public float rarity = 1;
    public float discardOnAirChance;
    public int size;
    public Color color;
    public boolean scattered;

    private OreUtil(PlacedFeature feature, int step, int index, Color color, RegistryKey<PlacedFeature> registryKey) {
        this.registryKey = registryKey; // Store the registry key
        this.step = step;
        this.index = index;
        this.color = color;
        int bottom = MinecraftClient.getInstance().world.getBottomY();
        int height = MinecraftClient.getInstance().world.getDimension().logicalHeight();
        this.heightContext = HeightContextUtil.createCustomHeightContext(bottom, height);

        for (PlacementModifier modifier : feature.placementModifiers()) {
            if (modifier instanceof CountPlacementModifier) {
                this.count = ((CPMAccess) modifier).getCount();

            } else if (modifier instanceof HeightRangePlacementModifier) {
                this.heightProvider = ((HRPMAccess) modifier).getHeight();

            } else if (modifier instanceof RarityFilterPlacementModifier) {
                this.rarity = ((RFPMAccess) modifier).getChance();
            }
        }

        FeatureConfig featureConfig = feature.feature().value().config();

        if (featureConfig instanceof OreFeatureConfig oreFeatureConfig) {
            this.discardOnAirChance = oreFeatureConfig.discardOnAirChance;
            this.size = oreFeatureConfig.size;
        } else {
            throw new IllegalStateException("config for " + feature + "is not OreFeatureConfig.class");
        }

        if (feature.feature().value().feature() instanceof ScatteredOreFeature) {
            this.scattered = true;
        }
    }
}