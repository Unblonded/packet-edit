package unblonded.packets.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import imgui.type.*;
import net.minecraft.client.MinecraftClient;
import unblonded.packets.Packetedit;
import unblonded.packets.cfg;
import unblonded.packets.util.BlockColor;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";

    // Create Gson with custom Optional adapter
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

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
                } else if (value instanceof List) {
                    // Handle List<BlockColor> specifically
                    if (fieldName.equals("espBlockList")) {
                        configJson.add(fieldName, gson.toJsonTree(value));
                    }
                } else if (value instanceof Boolean || value instanceof Integer ||
                        value instanceof Float || value instanceof String) {
                    configJson.add(fieldName, gson.toJsonTree(value));
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
                } else if (currentValue instanceof List && fieldName.equals("espBlockList")) {
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
            }

            System.out.println("Config loaded from: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveConfigAs(String fileName) {
        try {
            File configDir = Packetedit.workDir();

            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            // Ensure .json extension
            if (!fileName.endsWith(".json")) {
                fileName += ".json";
            }

            File configFile = new File(configDir, fileName);
            JsonObject configJson = new JsonObject();

            Field[] fields = cfg.class.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(null);

                // Skip Optional fields - handle them separately if needed
                if (value instanceof Optional) {
                    Optional<?> optional = (Optional<?>) value;
                    if (optional.isPresent()) {
                        configJson.add(fieldName, gson.toJsonTree(optional.get()));
                    }
                    continue;
                }

                // Same conversion logic as saveConfig()
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
                } else if (value instanceof int[] || value instanceof float[] || value instanceof String[]) {
                    configJson.add(fieldName, gson.toJsonTree(value));
                } else if (value instanceof ImInt[]) {
                    ImInt[] imIntArray = (ImInt[]) value;
                    int[] intArray = new int[imIntArray.length];
                    for (int i = 0; i < imIntArray.length; i++) {
                        intArray[i] = imIntArray[i].get();
                    }
                    configJson.add(fieldName, gson.toJsonTree(intArray));
                } else if (value instanceof List && fieldName.equals("espBlockList")) {
                    configJson.add(fieldName, gson.toJsonTree(value));
                } else if (value instanceof Boolean || value instanceof Integer ||
                        value instanceof Float || value instanceof String) {
                    configJson.add(fieldName, gson.toJsonTree(value));
                }
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(configJson, writer);
            }

            System.out.println("Config saved as: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to save config as " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadConfigFrom(String fileName) {
        try {
            File configDir = Packetedit.workDir();

            if (!fileName.endsWith(".json")) {
                fileName += ".json";
            }

            File configFile = new File(configDir, fileName);

            if (!configFile.exists()) {
                System.out.println("Config file " + fileName + " doesn't exist");
                return;
            }

            String jsonString = new String(Files.readAllBytes(configFile.toPath()));
            JsonObject configJson = JsonParser.parseString(jsonString).getAsJsonObject();

            Field[] fields = cfg.class.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if (!configJson.has(fieldName)) {
                    continue;
                }

                Object currentValue = field.get(null);
                JsonElement jsonElement = configJson.get(fieldName);

                // Handle Optional fields
                if (currentValue instanceof Optional) {
                    if (jsonElement.isJsonNull()) {
                        field.set(null, Optional.empty());
                    } else {
                        // You'll need to determine the type and create the Optional accordingly
                        field.set(null, Optional.of(jsonElement));
                    }
                    continue;
                }

                // Same loading logic as loadConfig()
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
                } else if (currentValue instanceof List && fieldName.equals("espBlockList")) {
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
            }

            System.out.println("Config loaded from: " + configFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to load config from " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<String> getAvailableConfigs() {
        List<String> configs = new ArrayList<>();
        try {
            File configDir = Packetedit.workDir();

            if (configDir.exists()) {
                File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File file : files) {
                        configs.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get available configs: " + e.getMessage());
        }
        return configs;
    }
}