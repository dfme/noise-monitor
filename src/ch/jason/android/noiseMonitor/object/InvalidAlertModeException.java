/**
 *
 */
package ch.jason.android.noiseMonitor.object;

/**
 * Thrown when a {@link AlertMode} is unknown.
 *
 * @author j
 */
public class InvalidAlertModeException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new invalid alert mode exception.
     *
     * @param msg the msg
     */
    public InvalidAlertModeException(String msg) {
        super(msg);
    }
}
