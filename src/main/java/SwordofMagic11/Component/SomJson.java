package SwordofMagic11.Component;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static SwordofMagic11.Component.Function.scale;

public class SomJson {
    private JsonObject root;

    public SomJson() {
        root = new JsonObject();
    }

    public SomJson(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            root = new Gson().fromJson(reader, JsonObject.class);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (root == null) root = new JsonObject();
    }

    public SomJson(String json) {
        root = new Gson().fromJson(json, JsonObject.class);
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    public JsonElement get(String key) {
        String[] keys = key.split("\\.");
        JsonElement element = null;
        if (getRoot().has(keys[0])) {
            element = getRoot().get(keys[0]);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                for (int i = 1; i < keys.length; i++) {
                    if (object.has(keys[i])) {
                        element = object.get(keys[i]);
                    }
                }
            }
        }
        return element;
    }

    public JsonObject getObject(String key) {
        String[] keys = key.split("\\.");
        JsonObject object = root;
        for (String keyData : keys) {
            if (!object.has(keyData)) {
                object.add(keyData, new JsonObject());
            }
            object = object.get(keyData).getAsJsonObject();
        }
        return object;
    }

    public JsonObject objectPath(String key) {
        String[] keys = key.split("\\.");
        int length = (keys.length - 1);
        if (length == 0) {
            return getRoot();
        }
        StringBuilder objKey = new StringBuilder(keys[0]);
        for (int i = 1; i < length; i++) {
            objKey.append(".").append(keys[i]);
        }
        return getObject(objKey.toString());
    }

    public String lastKey(String key) {
        String[] keys = key.split("\\.");
        return keys[keys.length - 1];
    }

    public boolean has(String key) {
        return objectPath(key).has(lastKey(key));
    }

    public void deleteKey(String key) {
        objectPath(key).remove(lastKey(key));
    }

    public void set(String key, String value) {
        objectPath(key).addProperty(lastKey(key), value);
    }

    public SomJson set(String key, int value) {
        objectPath(key).addProperty(lastKey(key), value);
        return this;
    }

    public SomJson set(String key, long value) {
        objectPath(key).addProperty(lastKey(key), value);
        return this;
    }

    public SomJson set(String key, double value) {
        return set(key, value, 4);
    }

    public SomJson set(String key, double value, int scale) {
        objectPath(key).addProperty(lastKey(key), scale(value, scale));
        return this;
    }

    public SomJson set(String key, float value) {
        return set(key, value, 4);
    }

    public SomJson set(String key, float value, int scale) {
        objectPath(key).addProperty(lastKey(key), scale(value, scale));
        return this;
    }

    public SomJson set(String key, boolean value) {
        objectPath(key).addProperty(lastKey(key), value);
        return this;
    }

    public SomJson set(String key, SomJson value) {
        objectPath(key).add(lastKey(key), value.getRoot());
        return this;
    }

    public JsonObject getRoot() {
        if (root == null) root = new JsonObject();
        return root;
    }

    public void save(File file) {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(root));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(String key) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsString() : "";
    }

    public String getString(String key, String defaultValue) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsString() : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsInt() : defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsLong() : defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsFloat() : defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsDouble() : defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? object.get(key).getAsBoolean() : defaultValue;
    }

    public JsonArray getArray(String key) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        if (!object.has(key) || object.isJsonArray()) {
            object.add(key, new JsonArray());
        }
        return object.get(key).getAsJsonArray();
    }

    public List<String> getList(String key) {
        List<String> list = new ArrayList<>();
        for (JsonElement element : getArray(key)) {
            list.add(element.toString());
        }
        return list;
    }

    public List<String> getStringList(String key) {
        List<String> list = new ArrayList<>();
        for (JsonElement element : getArray(key)) {
            list.add(element.getAsString());
        }
        return list;
    }

    public SomJson getSomJson(String key) {
        JsonObject object = objectPath(key);
        key = lastKey(key);
        return object.has(key) ? new SomJson(object.get(key).toString()) : new SomJson();
    }

    public void addArray(String key, SomJson json) {
        getArray(key).add(json.root);
    }

    public void addArray(String key, String value) {
        getArray(key).add(value);
    }

    public void addArray(String key, int value) {
        getArray(key).add(value);
    }

    public void addArray(String key, double value) {
        getArray(key).add(value);
    }

    public void addArray(String key, float value) {
        getArray(key).add(value);
    }

    public void addArray(String key, boolean value) {
        getArray(key).add(value);
    }


    @Override
    public String toString() {
        return root.toString();
    }
}
