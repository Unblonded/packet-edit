package unblonded.packets.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import imgui.type.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import unblonded.packets.Packetedit;
import unblonded.packets.cfg;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.rmi.registry.Registry;
import java.util.*;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";

    // Create Gson with custom adapters
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BlockColor.class, new BlockColorAdapter())
            .registerTypeAdapter(Color.class, new ColorAdapter())
            .registerTypeAdapter(KitSlot.class, new KitSlotAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    // Custom adapter for BlockColor that safely serializes Block objects
    private static class BlockColorAdapter implements JsonSerializer<BlockColor>, JsonDeserializer<BlockColor> {
        @Override
        public JsonElement serialize(BlockColor src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();

            // Serialize block as its registry name instead of the full object
            Block block = src.getBlock();
            if (block != null) {
                Identifier blockId = Registries.BLOCK.getId(block);
                obj.addProperty("block", blockId.toString());
            }

            // Serialize color using our custom adapter
            obj.add("color", context.serialize(src.getColor()));
            obj.addProperty("enabled", src.isEnabled());

            return obj;
        }

        @Override
        public BlockColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            // Deserialize block from registry name
            Block block = null;
            if (obj.has("block")) {
                String blockId = obj.get("block").getAsString();
                block = Registries.BLOCK.get(Identifier.of(blockId));
            }

            // Deserialize color
            Color color = context.deserialize(obj.get("color"), Color.class);

            boolean enabled = obj.has("enabled") ? obj.get("enabled").getAsBoolean() : true;

            return new BlockColor(block, color, enabled);
        }
    }

    // Custom adapter for Color class
    private static class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
        @Override
        public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("red", src.R());
            obj.addProperty("green", src.G());
            obj.addProperty("blue", src.B());
            obj.addProperty("alpha", src.A());
            return obj;
        }

        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            float r = obj.get("red").getAsFloat();
            float g = obj.get("green").getAsFloat();
            float b = obj.get("blue").getAsFloat();
            float a = obj.get("alpha").getAsFloat();
            return new Color(r, g, b, a);
        }
    }

    // Custom adapter for KitSlot that safely serializes Item objects
    private static class KitSlotAdapter implements JsonSerializer<KitSlot>, JsonDeserializer<KitSlot> {
        @Override
        public JsonElement serialize(KitSlot src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("slotIndex", src.slotIndex);

            // Serialize item as its registry name instead of the full object
            if (src.item != null) {
                Identifier itemId = Registries.ITEM.getId(src.item);
                obj.addProperty("item", itemId.toString());
            }

            return obj;
        }

        @Override
        public KitSlot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            int slotIndex = obj.get("slotIndex").getAsInt();

            // Deserialize item from registry name
            Item item = null;
            if (obj.has("item")) {
                String itemId = obj.get("item").getAsString();
                item = Registries.ITEM.get(Identifier.of(itemId));
            }

            return new KitSlot(slotIndex, item);
        }
    }

    // Custom TypeAdapter for Optional
    private static class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {
        private final TypeAdapter<T> adapter;

        public OptionalTypeAdapter(TypeAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void write(JsonWriter out, Optional<T> value) throws IOException {
            if (value.isPresent()) {
                adapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }

        @Override
        public Optional<T> read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return Optional.empty();
            }
            return Optional.of(adapter.read(in));
        }
    }

    // TypeAdapterFactory for Optional
    private static class OptionalTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (rawType != Optional.class) {
                return null;
            }

            Type[] typeArguments = ((java.lang.reflect.ParameterizedType) type.getType()).getActualTypeArguments();
            Type elementType = typeArguments[0];
            TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));

            return (TypeAdapter<T>) new OptionalTypeAdapter<>(elementAdapter);
        }
    }

    public static void saveConfig() {
        try {
            // Get .minecraft directory
            File configDir = Packetedit.workDir();

            // Create directory if it doesn't exist
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            //THINGS TO SET FALSE
            cfg.triggerAutoSell = false;
            cfg.autoDcPrimed.set(false);

            File configFile = new File(configDir, CONFIG_FILE);
            JsonObject configJson = new JsonObject();

            // Use reflection to get all fields from cfg class
            Field[] fields = cfg.class.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(null);

                try {
                    // Skip Optional fields - handle them separately if needed
                    if (value instanceof Optional) {
                        Optional<?> optional = (Optional<?>) value;
                        if (optional.isPresent()) {
                            configJson.add(fieldName, gson.toJsonTree(optional.get()));
                        }
                        continue;
                    }

                    // Convert different types to JSON
                    if (value instanceof ImBoolean) {
                        configJson.addProperty(fieldName, ((ImBoolean) value).get());
                    } else if (value instanceof ImInt) {
                        configJson.addProperty(fieldName, ((ImInt) value).get());
                    } else if (value instanceof ImFloat) {
                        configJson.addProperty(fieldName, ((ImFloat) value).get());
                    } else if (value instanceof ImLong) {
                        configJson.addProperty(fieldName, ((ImLong) value).get());
                    } else if (value instanceof ImString) {
                        configJson.addProperty(fieldName, ((ImString) value).get());
                    } else if (value instanceof int[]) {
                        configJson.add(fieldName, gson.toJsonTree(value));
                    } else if (value instanceof float[]) {
                        configJson.add(fieldName, gson.toJsonTree(value));
                    } else if (value instanceof String[]) {
                        configJson.add(fieldName, gson.toJsonTree(value));
                    } else if (value instanceof ImInt[]) {
                        ImInt[] imIntArray = (ImInt[]) value;
                        int[] intArray = new int[imIntArray.length];
                        for (int i = 0; i < imIntArray.length; i++) {
                            intArray[i] = imIntArray[i].get();
                        }
                        configJson.add(fieldName, gson.toJsonTree(intArray));
                    } else if (value instanceof Map && fieldName.equals("savedLoadouts")) {
                        // Handle Map<String, List<KitSlot>> with our custom adapter
                        configJson.add(fieldName, gson.toJsonTree(value, new TypeToken<Map<String, List<KitSlot>>>(){}.getType()));
                    } else if (value instanceof List) {
                        // Now we can safely handle List<BlockColor> with our custom adapter
                        if (fieldName.equals("espBlockList")) {
                            configJson.add(fieldName, gson.toJsonTree(value, new TypeToken<List<BlockColor>>(){}.getType()));
                        } else {
                            // Handle other list types
                            configJson.add(fieldName, gson.toJsonTree(value));
                        }
                    } else if (value instanceof Boolean || value instanceof Integer ||
                            value instanceof Float || value instanceof String) {
                        configJson.add(fieldName, gson.toJsonTree(value));
                    }
                } catch (Exception e) {
                    System.err.println("Error serializing field " + fieldName + ": " + e.getMessage());
                    e.printStackTrace();
                    // Continue with other fields
                }
            }

            // Write to file
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(configJson, writer);
            }

            System.out.println("Config saved to: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
        try {
            File configDir = Packetedit.workDir();
            File configFile = new File(configDir, CONFIG_FILE);

            if (!configFile.exists()) {
                System.out.println("Config file doesn't exist, using defaults");
                return;
            }

            // Read JSON from file
            String jsonString = new String(Files.readAllBytes(configFile.toPath()));
            JsonObject configJson = JsonParser.parseString(jsonString).getAsJsonObject();

            // Use reflection to set all fields in cfg class
            Field[] fields = cfg.class.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if (!configJson.has(fieldName)) {
                    continue; // Skip if not in config file
                }

                Object currentValue = field.get(null);
                JsonElement jsonElement = configJson.get(fieldName);

                try {
                    // Handle Optional fields
                    if (currentValue instanceof Optional) {
                        if (jsonElement.isJsonNull()) {
                            field.set(null, Optional.empty());
                        } else {
                            // You'll need to determine the type and create the Optional accordingly
                            // This is a simplified example - you may need more specific handling
                            field.set(null, Optional.of(jsonElement));
                        }
                        continue;
                    }

                    // Set values based on type
                    if (currentValue instanceof ImBoolean) {
                        ((ImBoolean) currentValue).set(jsonElement.getAsBoolean());
                    } else if (currentValue instanceof ImInt) {
                        ((ImInt) currentValue).set(jsonElement.getAsInt());
                    } else if (currentValue instanceof ImFloat) {
                        ((ImFloat) currentValue).set(jsonElement.getAsFloat());
                    } else if (currentValue instanceof ImLong) {
                        ((ImLong) currentValue).set(jsonElement.getAsLong());
                    } else if (currentValue instanceof ImString) {
                        ((ImString) currentValue).set(jsonElement.getAsString());
                    } else if (currentValue instanceof int[]) {
                        int[] array = gson.fromJson(jsonElement, int[].class);
                        field.set(null, array);
                    } else if (currentValue instanceof float[]) {
                        float[] array = gson.fromJson(jsonElement, float[].class);
                        field.set(null, array);
                    } else if (currentValue instanceof String[]) {
                        String[] array = gson.fromJson(jsonElement, String[].class);
                        field.set(null, array);
                    } else if (currentValue instanceof ImInt[]) {
                        int[] intArray = gson.fromJson(jsonElement, int[].class);
                        ImInt[] imIntArray = new ImInt[intArray.length];
                        for (int i = 0; i < intArray.length; i++) {
                            imIntArray[i] = new ImInt(intArray[i]);
                        }
                        field.set(null, imIntArray);
                    } else if (currentValue instanceof Map && fieldName.equals("savedLoadouts")) {
                        // Handle Map<String, List<KitSlot>> deserialization
                        Map<String, List<KitSlot>> loadouts = gson.fromJson(jsonElement,
                                new TypeToken<Map<String, List<KitSlot>>>(){}.getType());

                        // Clear the existing map and repopulate it
                        ((Map<String, List<KitSlot>>) currentValue).clear();
                        ((Map<String, List<KitSlot>>) currentValue).putAll(loadouts);
                    } else if (currentValue instanceof List && fieldName.equals("espBlockList")) {
                        // Now we can safely deserialize List<BlockColor>
                        List<BlockColor> blockList = gson.fromJson(jsonElement,
                                new TypeToken<List<BlockColor>>(){}.getType());
                        field.set(null, blockList);
                    } else if (currentValue instanceof Boolean) {
                        field.setBoolean(null, jsonElement.getAsBoolean());
                    } else if (currentValue instanceof Integer) {
                        field.setInt(null, jsonElement.getAsInt());
                    } else if (currentValue instanceof Float) {
                        field.setFloat(null, jsonElement.getAsFloat());
                    } else if (currentValue instanceof String) {
                        field.set(null, jsonElement.getAsString());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading field " + fieldName + ": " + e.getMessage());
                    e.printStackTrace();
                    // Continue with other fields
                }
            }

            System.out.println("Config loaded from: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}