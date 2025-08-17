package com.momosoftworks.prospect.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonHelper
{
    public static ObjectNode createObjectBuilder()
    {   ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }

    public static ArrayNode createArrayBuilder()
    {   ObjectMapper mapper = new ObjectMapper();
        return mapper.createArrayNode();
    }

    public static String getString(ObjectNode obj, String key, String defaultValue)
    {
        if (obj.has(key) && obj.get(key).isTextual())
        {   return obj.get(key).asText();
        }
        return defaultValue;
    }
    public static String getString(ObjectNode obj, String key)
    {   return getString(obj, key, "");
    }
    public static String getString(ArrayNode obj, int index)
    {
        if (index >= 0 && index < obj.size() && obj.get(index).isTextual())
        {   return obj.get(index).asText();
        }
        return "";
    }

    public static int getInt(ObjectNode obj, String key, int defaultValue)
    {
        if (obj.has(key) && obj.get(key).isInt())
        {   return obj.get(key).asInt();
        }
        return defaultValue;
    }
    public static int getInt(ObjectNode obj, String key)
    {   return getInt(obj, key, 0);
    }
    public static int getInt(ArrayNode obj, int index, int defaultValue)
    {
        if (index >= 0 && index < obj.size() && obj.get(index).isInt())
        {   return obj.get(index).asInt();
        }
        return defaultValue;
    }

    public static long getLong(ObjectNode obj, String key, long defaultValue)
    {
        if (obj.has(key) && obj.get(key).isLong())
        {   return obj.get(key).asLong();
        }
        return defaultValue;
    }
    public static long getLong(ObjectNode obj, String key)
    {   return getLong(obj, key, 0L);
    }
    public static long getLong(ArrayNode obj, int index, long defaultValue)
    {
        if (index >= 0 && index < obj.size() && obj.get(index).isLong())
        {   return obj.get(index).asLong();
        }
        return defaultValue;
    }

    public static double getDouble(ObjectNode obj, String key, double defaultValue)
    {
        if (obj.has(key) && obj.get(key).isDouble())
        {   return obj.get(key).asDouble();
        }
        return defaultValue;
    }
    public static double getDouble(ObjectNode obj, String key)
    {   return getDouble(obj, key, 0.0);
    }
    public static double getDouble(ArrayNode obj, int index, double defaultValue)
    {
        if (index >= 0 && index < obj.size() && obj.get(index).isDouble())
        {   return obj.get(index).asDouble();
        }
        return defaultValue;
    }

    public static boolean getBoolean(ObjectNode obj, String key, boolean defaultValue)
    {
        if (obj.has(key) && obj.get(key).isBoolean())
        {   return obj.get(key).asBoolean();
        }
        return defaultValue;
    }
    public static boolean getBoolean(ObjectNode obj, String key)
    {   return getBoolean(obj, key, false);
    }
    public static boolean getBoolean(ArrayNode obj, int index, boolean defaultValue)
    {
        if (index >= 0 && index < obj.size() && obj.get(index).isBoolean())
        {   return obj.get(index).asBoolean();
        }
        return defaultValue;
    }

    public static ArrayNode getJsonArray(ObjectNode obj, String key)
    {
        if (obj.has(key) && obj.get(key).isArray())
        {   return (ArrayNode) obj.get(key);
        }
        return createArrayBuilder();
    }
    public static ArrayNode getJsonArray(ObjectNode obj, String key, ArrayNode defaultValue)
    {
        if (obj.has(key) && obj.get(key).isArray())
        {   return (ArrayNode) obj.get(key);
        }
        return defaultValue;
    }
}
