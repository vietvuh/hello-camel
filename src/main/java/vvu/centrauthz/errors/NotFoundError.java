package vvu.centrauthz.errors;

import vvu.centrauthz.models.Error;

/**
 * Not Found Error.
 */
public class NotFoundError extends AppError {
    public NotFoundError(Error error) {
        super(error);
    }

    public NotFoundError(String code, String message) {
        super(code, message);
    }

    public NotFoundError(String code) {
        super(code);
    }
}
