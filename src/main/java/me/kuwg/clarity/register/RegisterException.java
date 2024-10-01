package me.kuwg.clarity.register;

/**
 * A custom unchecked exception that is thrown by the {@link Register} class
 * after it encounters an error and exits. This exception is primarily used for
 * documentation purposes to provide clarity in the Javadoc regarding exceptions
 * raised by methods in the {@code Register} class.
 * <p>
 * The {@code RegisterException} is not meant to be caught or handled; it is used
 * internally to indicate a critical failure in the Clarity interpreter's register
 * operations.
 * </p>
 *
 * @author NotKuwg
 * @see Register#raise()
 */
@SuppressWarnings("unused")
public final class RegisterException extends RuntimeException {

    /**
     * Constructs a new {@code RegisterException}.
     * <p>
     * This constructor is intentionally package-private and is only called
     * internally by the {@link Register} class when it needs to raise this exception.
     * </p>
     */
    RegisterException() {
    }
}