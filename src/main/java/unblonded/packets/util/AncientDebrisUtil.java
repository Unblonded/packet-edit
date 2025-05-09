package unblonded.packets.util;

import unblonded.packets.mixin.CPMAccess;
import unblonded.packets.mixin.HRPMAccess;
import unblonded.packets.mixin.RFPMAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.*;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.*;

/**
 * Utility class for Ancient Debris generation calculations
 */
public class AncientDebrisUtil {
    public int step;
    public int index;
    public IntProvider count = ConstantIntProvider.create(1);
    public HeightProvider heightProvider;
    public HeightContext heightContext;
    public float rarity = 1;
    public float discardOnAirChance;
    public int size;
    public boolean scattered;

    public AncientDebrisUtil(PlacedFeature feature, int step, int index) {
        this.step = step;
        this.index = index;

        // Initialize height context
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.world != null) {
            int bottom = mc.world.getBottomY();
            int height = mc.world.getDimension().logicalHeight();
            this.heightContext = HeightContextUtil.createCustomHeightContext(bottom, height);
            //this.heightContext = new HeightContext(null, HeightLimitView.create(bottom, height));
        }

        // Process all placement modifiers
        for (PlacementModifier modifier : feature.placementModifiers()) {
            if (modifier instanceof CountPlacementModifier) {
                this.count = ((CPMAccess) modifier).getCount();
            }
            else if (modifier instanceof HeightRangePlacementModifier) {
                this.heightProvider = ((HRPMAccess) modifier).getHeight();
            }
            else if (modifier instanceof RarityFilterPlacementModifier) {
                this.rarity = ((RFPMAccess) modifier).getChance();
            }
        }

        // Get feature configuration
        FeatureConfig featureConfig = feature.feature().value().config();

        if (featureConfig instanceof OreFeatureConfig oreFeatureConfig) {
            this.discardOnAirChance = oreFeatureConfig.discardOnAirChance;
            this.size = oreFeatureConfig.size;

            // Determine if this is scattered ore generation
            this.scattered = feature.feature().value().feature() instanceof ScatteredOreFeature;
        } else {
            throw new IllegalStateException("Expected OreFeatureConfig for " + feature);
        }
    }

    public static AncientDebrisUtil createForFeature(RegistryKey<PlacedFeature> featureKey, int step) {
        RegistryWrapper.WrapperLookup registry = BuiltinRegistries.createWrapperLookup();
        RegistryWrapper.Impl<PlacedFeature> features = registry.getOrThrow(RegistryKeys.PLACED_FEATURE);

        // Get the PlacedFeatureIndexer for the dimension
        var dimensions = registry.getOrThrow(RegistryKeys.WORLD_PRESET)
                .getOrThrow(WorldPresets.DEFAULT)
                .value()
                .createDimensionsRegistryHolder()
                .dimensions();
        var dim = dimensions.get(DimensionOptions.NETHER);

        List<PlacedFeatureIndexer.IndexedFeatures> indexer = PlacedFeatureIndexer.collectIndexedFeatures(
                dim.chunkGenerator().getBiomeSource().getBiomes().stream().toList(),
                biomeEntry -> biomeEntry.value().getGenerationSettings().getFeatures(),
                true
        );

        // Get the actual index from the indexer
        PlacedFeature feature = features.getOrThrow(featureKey).value();
        int index = indexer.get(step).indexMapping().applyAsInt(feature);

        return new AncientDebrisUtil(feature, step, index);
    }
}