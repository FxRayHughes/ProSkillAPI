/**
 * SkillAPI
 * com.sucy.skill.serialization.gson.GsonUtils
 * <p>
 * The MIT License (MIT)
 */
package com.sucy.skill.serialization.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.rit.sucy.config.parse.DataSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gson setup for Bukkit's ConfigurationSerializable hierarchy. Bukkit item
 * metadata contains nested serializable objects, so a plain Gson instance would
 * lose the type alias required by ConfigurationSerialization on restore.
 */
public final class GsonUtils {
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Gson GSON = createGson();

    private GsonUtils() {
        // Utility class: one immutable Gson instance is enough for all item data.
    }

    /**
     * @return Gson configured for Bukkit serializable objects and integer-safe maps
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Serializes every persisted value through the single Gson instance used by
     * the plugin. DataSection is normalized first because it is an in-memory
     * configuration tree, not a persistence format and should never be emitted
     * through its YAML-oriented toString method.
     *
     * @param value value to serialize
     * @return JSON document
     */
    public static String toJson(Object value) {
        return GSON.toJson(normalize(value));
    }

    /**
     * Deserializes a JSON document using the shared Bukkit-aware Gson instance.
     *
     * @param json JSON document
     * @param type destination class
     * @param <T> destination type
     * @return deserialized value, or null for a blank document
     */
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, type);
    }

    /**
     * Type-aware variant used for generic DTOs and maps.
     *
     * @param json JSON document
     * @param type destination type
     * @param <T> destination type
     * @return deserialized value
     */
    public static <T> T fromJson(String json, Type type) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, type);
    }

    /**
     * Deserializes a generic map through the shared Gson configuration. This is
     * used by file and database migration code so no caller creates a second
     * Gson instance with subtly different Bukkit handling.
     *
     * @param values source map
     * @param type destination type
     * @param <T> destination type
     * @return converted value
     */
    public static <T> T fromMap(Map<String, Object> values, Type type) {
        return values == null ? null : GSON.fromJson(GSON.toJson(values), type);
    }

    /**
     * Reads a UTF-8 JSON document from disk.
     *
     * @param file JSON file
     * @return JSON text, or null when the file does not exist
     * @throws IOException when the file cannot be read
     */
    public static String readJson(File file) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }
        return new String(Files.readAllBytes(file.toPath()), UTF_8);
    }

    /**
     * Writes a JSON document atomically. The temporary file and replacement
     * avoid leaving a truncated player/skill document after a server stop.
     *
     * @param file destination JSON file
     * @param value value to serialize
     * @throws IOException when the file cannot be written
     */
    public static void writeJson(File file, Object value) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("JSON destination cannot be null");
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Unable to create JSON directory: " + parent);
        }
        // resolveSibling keeps the temporary file next to the target without
        // assuming there is a parent: a relative path like "skills.json" has a
        // null getParentFile(), which the File constructor would reject.
        Path target = file.toPath();
        Path temporary = target.resolveSibling(file.getName() + ".tmp");
        Files.write(temporary, toJson(value).getBytes(UTF_8));
        try {
            Files.move(temporary, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Converts a value into a plain Map/List tree before persistence. The
     * method intentionally keeps Bukkit ConfigurationSerializable values as
     * objects so the registered adapter can add and later resolve its "=="
     * type alias.
     *
     * @param value value to normalize
     * @return JSON-compatible map, or null when the root is not a map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object value) {
        Object parsed = deepParse(GSON.toJsonTree(normalize(value)));
        return parsed instanceof Map ? (Map<String, Object>) parsed : null;
    }

    /**
     * Parses JSON into ordinary maps/lists while preserving integral values.
     * This helper is kept public because migration code may need to inspect
     * serialized Bukkit payloads without binding to a concrete item class.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> deepDeserialize(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        Object value = restoreConfigurationObjects(deepParse(GSON.fromJson(json, JsonElement.class)));
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    /**
     * Rebuilds the legacy configuration tree from JSON for the existing player
     * loading code. This is a compatibility bridge only; new data is stored as
     * JSON and never converted back to YAML text.
     *
     * @param json JSON document
     * @return configuration tree or null when the document is not an object
     */
    public static DataSection toDataSection(String json) {
        return toDataSection(deepDeserialize(json));
    }

    /**
     * Rebuilds a configuration tree from a plain map while retaining nested
     * lists, numbers, and Bukkit serializable objects.
     *
     * @param values source map
     * @return configuration tree or null for a null map
     */
    public static DataSection toDataSection(Map<String, Object> values) {
        if (values == null) {
            return null;
        }
        DataSection section = new DataSection();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            section.set(entry.getKey(), normalizeForDataSection(entry.getValue()));
        }
        return section;
    }

    private static Object normalize(Object value) {
        if (value instanceof DataSection) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> entry : ((DataSection) value).entrySet()) {
                map.put(entry.getKey(), normalize(entry.getValue()));
            }
            return map;
        }
        if (value instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                map.put(String.valueOf(entry.getKey()), normalize(entry.getValue()));
            }
            return map;
        }
        if (value instanceof Iterable) {
            List<Object> list = new ArrayList<Object>();
            for (Object child : (Iterable<?>) value) {
                list.add(normalize(child));
            }
            return list;
        }
        if (value != null && value.getClass().isArray()) {
            List<Object> list = new ArrayList<Object>(Array.getLength(value));
            for (int i = 0; i < Array.getLength(value); i++) {
                list.add(normalize(Array.get(value, i)));
            }
            return list;
        }
        return value;
    }

    private static Object normalizeForDataSection(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                map.put(String.valueOf(entry.getKey()), normalizeForDataSection(entry.getValue()));
            }
            return map;
        }
        if (value instanceof List) {
            List<Object> list = new ArrayList<Object>(((List<?>) value).size());
            for (Object child : (List<?>) value) {
                list.add(normalizeForDataSection(child));
            }
            return list;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Object restoreConfigurationObjects(Object value) {
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            for (Map.Entry<Object, Object> entry : new ArrayList<Map.Entry<Object, Object>>(map.entrySet())) {
                Object child = restoreConfigurationObjects(entry.getValue());
                if (child instanceof Map
                        && ((Map<?, ?>) child).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    try {
                        child = ConfigurationSerialization.deserializeObject((Map<String, Object>) child);
                    } catch (Exception ignored) {
                        // Optional Bukkit serializers may not exist on every core.
                    }
                }
                entry.setValue(child);
            }
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (int i = 0; i < list.size(); i++) {
                Object child = restoreConfigurationObjects(list.get(i));
                if (child instanceof Map
                        && ((Map<?, ?>) child).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    try {
                        child = ConfigurationSerialization.deserializeObject((Map<String, Object>) child);
                    } catch (Exception ignored) {
                        // Keep the raw map when the optional type is unavailable.
                    }
                }
                list.set(i, child);
            }
        }
        return value;
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new BukkitSerializableAdapterFactory())
                .registerTypeAdapterFactory(MapTypeAdapter.FACTORY)
                .disableHtmlEscaping()
                .serializeNulls()
                .create();
    }

    private static Object deepParse(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                map.put(entry.getKey(), deepParse(entry.getValue()));
            }
            return map;
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<Object>();
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                list.add(deepParse(child));
            }
            return list;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            String value = primitive.getAsString();
            if (value.indexOf('.') >= 0 || value.indexOf('e') >= 0 || value.indexOf('E') >= 0) {
                return Double.valueOf(value);
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignored) {
                return Long.valueOf(value);
            }
        }
        return primitive.getAsString();
    }

    private static final class BukkitSerializableAdapterFactory implements TypeAdapterFactory {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!ConfigurationSerializable.class.isAssignableFrom(type.getRawType())) {
                return null;
            }
            return (TypeAdapter<T>) new BukkitSerializableAdapter(gson).nullSafe();
        }
    }

    /**
     * Adds Bukkit's "==" alias to serialized objects and resolves nested aliases
     * before calling the official ConfigurationSerialization registry.
     */
    private static final class BukkitSerializableAdapter extends TypeAdapter<ConfigurationSerializable> {
        private final Gson gson;

        private BukkitSerializableAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, ConfigurationSerializable value) throws IOException {
            Map<String, Object> serialized = value.serialize();
            Map<String, Object> map = new LinkedHashMap<String, Object>(serialized.size() + 1);
            map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                    ConfigurationSerialization.getAlias(value.getClass()));
            map.putAll(serialized);
            gson.toJson(map, MAP_TYPE, out);
        }

        @Override
        public ConfigurationSerializable read(JsonReader in) throws IOException {
            Map<String, Object> map = gson.fromJson(in, MAP_TYPE);
            deserializeChildren(map);
            return ConfigurationSerialization.deserializeObject(map);
        }

        private void deserializeChildren(Object value) {
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object child = entry.getValue();
                    deserializeChildren(child);
                    if (child instanceof Map && ((Map<?, ?>) child).containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                        try {
                            Object converted = ConfigurationSerialization.deserializeObject(
                                    (Map<String, Object>) child);
                            ((Map<Object, Object>) map).put(entry.getKey(), converted);
                        } catch (Exception ignored) {
                            // Keep the raw map when an optional Bukkit type is unavailable.
                        }
                    }
                }
            } else if (value instanceof List) {
                for (Object child : (List<?>) value) {
                    deserializeChildren(child);
                }
            }
        }
    }

    /**
     * Gson's Object adapter otherwise turns every integer in a raw Bukkit map
     * into a Double. Bukkit accepts Number values, but preserving integers keeps
     * old metadata and custom model data migrations deterministic.
     */
    private static final class MapTypeAdapter extends TypeAdapter<Object> {
        private static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == Object.class) {
                    return (TypeAdapter<T>) new MapTypeAdapter(gson);
                }
                return null;
            }
        };

        private final Gson gson;

        private MapTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;
                case BEGIN_OBJECT:
                    // LinkedHashMap, not Gson's internal LinkedTreeMap: both
                    // preserve insertion order, and this one is a stable API.
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;
                case STRING:
                    return in.nextString();
                case NUMBER:
                    String number = in.nextString();
                    if (number.indexOf('.') >= 0 || number.indexOf('e') >= 0 || number.indexOf('E') >= 0) {
                        return Double.valueOf(number);
                    }
                    try {
                        return Integer.valueOf(number);
                    } catch (NumberFormatException ignored) {
                        return Long.valueOf(number);
                    }
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    throw new IllegalStateException("Unsupported JSON token: " + in.peek());
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void write(JsonWriter out, Object value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            Class<?> runtimeType = value.getClass();
            // A value whose runtime type is plain Object would resolve back to
            // this very adapter and recurse forever. Detecting that by identity
            // is what removes the old dependency on Gson's internal
            // ObjectTypeAdapter class, whose name and behaviour are not API.
            if (runtimeType == Object.class) {
                out.beginObject();
                out.endObject();
                return;
            }
            TypeAdapter<Object> adapter = (TypeAdapter<Object>) gson.getAdapter(runtimeType);
            if (adapter == this) {
                out.beginObject();
                out.endObject();
                return;
            }
            adapter.write(out, value);
        }
    }
}
