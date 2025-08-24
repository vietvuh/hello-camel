package vvu.centrauthz.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vvu.centrauthz.errors.IllegalJsonValue;

import java.util.function.Supplier;

/**
 * Utility class for JSON operations and ObjectMapper access.
 */
public class JsonTools {

    interface JSupplier<T, R extends Exception> {
        T get() throws R;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    static <T> T jsonContext(JSupplier<T, Exception> supplier) {
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

    public static String toString(Object node) {
        return jsonContext(() -> mapper().writeValueAsString(node));
    }

}
