package vvu.centrauthz.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import vvu.centrauthz.errors.IllegalJsonValue;

/**
 * Utility class for JSON operations and ObjectMapper access.
 */
public class JsonTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    interface JsonSupplier<T, E extends Exception> {
        /**
         * Retrieves the result of the supplier.
         *
         * @return the result of the supplier
         * @throws E exception that may occur during supplier execution
         */
        T get() throws E;
    }

    /**
     * Private constructor to prevent instantiation.
     */
    JsonTools() {
        throw new IllegalStateException();
    }

    /**
     * Executes a supplier within a JSON context, wrapping exceptions.
     *
     * @param <T> the return type of the supplier
     * @param supplier the operation to execute
     * @return the result of the supplier
     * @throws IllegalJsonValue if any exception occurs during execution
     */
    static <T> T jsonContext(JsonSupplier<T, Exception> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new IllegalJsonValue(e);
        }
    }

    /**
     * Returns the shared ObjectMapper instance.
     *
     * @return the configured ObjectMapper
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static JsonNode from(String v) {
        return jsonContext(() -> mapper().readTree(v));
    }

    public static JsonNode toJsonOrString(String v) {
        try {
            return mapper().readTree(v);
        } catch (JsonProcessingException e) {
            return mapper().valueToTree(v);
        }
    }
}
