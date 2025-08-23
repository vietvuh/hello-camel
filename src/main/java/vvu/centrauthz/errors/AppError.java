package vvu.centrauthz.errors;

import java.io.Serial;
import lombok.Getter;
import vvu.centrauthz.models.Error;

/**
 * Application-specific error exception that carries an Error response.
 */
@Getter
public class AppError extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final transient Error error;

    /**
     * Constructor with Error.
     *
     * @param error error
     */
    public AppError(Error error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Constructor with code.
     *
     * @param code Error Code
     */
    public AppError(String code) {
        super(code);
        this.error = Error.builder()
            .code(code)
            .build();
    }

    /**
     * Constructor with code and exception.
     *
     * @param code Error Code
     * @param e Throwable
     */
    public AppError(String code, Throwable e) {
        super(e);
        this.error = Error.builder()
            .code(code)
            .message(e.getMessage())
            .build();
    }

    /**
     * Constructor with code and message.
     *
     * @param code Error Code
     * @param message error message
     */
    public AppError(String code, String message) {
        super(message);
        this.error = Error.builder()
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public String toString() {
        return "{ error=" + error + '}';
    }
}
