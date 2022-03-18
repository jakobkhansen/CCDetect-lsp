package CCDetect.lsp.utils;


import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * JSONUtility
 */
public class JSONUtility {

	/**
	 * Converts given JSON objects to given Model objects.
	 *
	 * @throws IllegalArgumentException if clazz is null
	 */
	public static <T> T toModel(Object object, Class<T> clazz){
		if(object == null){
			return null;
		}
		if(clazz == null ){
			throw new IllegalArgumentException("Class can not be null");
		}
		if(object instanceof JsonElement){
			Gson gson = new Gson();
			return gson.fromJson((JsonElement) object, clazz);
		}
		if (clazz.isInstance(object)) {
			return clazz.cast(object);
		}
		if (object instanceof String) {
			Gson gson = new Gson();
			return gson.fromJson((String) object, clazz);
		}
		return null;
	}


}
