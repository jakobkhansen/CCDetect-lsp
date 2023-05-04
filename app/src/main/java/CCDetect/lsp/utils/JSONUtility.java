package CCDetect.lsp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import CCDetect.lsp.server.Configuration;

import java.util.logging.Logger;

/**
 * JSONUtility
 */
public class JSONUtility {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Converts given JSON objects to given Model objects.
     *
     * @throws IllegalArgumentException if clazz is null
     */
    public static <T> T toModel(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Class can not be null");
        }
        if (object instanceof JsonElement) {
            Gson gson = new Gson();
            return gson.fromJson((JsonElement) object, clazz);
        }
        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        if (object instanceof String) {
            LOGGER.info("gson parsing: " + ((String) object));
            JsonObject test = new JsonObject();
            Gson gson = new Gson();
            T out = gson.fromJson((String) object, clazz);
            LOGGER.info("gson here: " + ((Configuration) out).getCloneTokenThreshold());

            return out;
        }
        return null;
    }

}
