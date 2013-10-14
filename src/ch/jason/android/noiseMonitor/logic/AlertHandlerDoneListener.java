/**
 *
 */
package ch.jason.android.noiseMonitor.logic;

/**
 * An interface which describes how an alert handler can notify any registered
 * listeners.
 *
 * @author j
 */
public interface AlertHandlerDoneListener {

    public static int RESUME_CODE = 0;

    public static int STOP_CODE = 1;

    /**
     * Called when the listener should be notified.
     *
     * @param code possible values: {@link AlertHandlerDoneListener#RESUME_CODE} or
     *             {@link AlertHandlerDoneListener#STOP_CODE}
     */
    public void done(int code);
}
