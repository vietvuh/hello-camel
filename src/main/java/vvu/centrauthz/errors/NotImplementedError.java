package vvu.centrauthz.errors;

import vvu.centrauthz.models.Error;

/**
 * Not Implemented Error.
 */
public class NotImplementedError extends AppError {
    public NotImplementedError(Error e) {
        super(e);
    }

    public NotImplementedError(String message) {
        super("NOT_IMPLEMENTED", message);
    }

    public NotImplementedError(Throwable e) {
        super("NOT_IMPLEMENTED", e);
    }
}
