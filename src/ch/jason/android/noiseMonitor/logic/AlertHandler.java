/**
 *
 */
package ch.jason.android.noiseMonitor.logic;

import android.content.Context;

/**
 * An Interface which describes how an alert handler can be called.
 *
 * @author j
 */
public interface AlertHandler {

    /**
     * Execute an alert.
     *
     * @param context the service instance which is calling the alert
     */
    public void alert(Context context);

    /**
     * Register a listener which is notified as soon as the alert is finished.
     *
     * @param listener the listener
     */
    public void registerListener(AlertHandlerDoneListener listener);

    /**
     * Notify listeners.
     * <p>
     * Call this as soon as the alert handler is done so that any registered
     * listeners can be notified.
     * </p>
     *
     * @param code the code
     */
    void notifyListeners(int code);
}
