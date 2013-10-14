/**
 *
 */
package ch.jason.android.noiseMonitor.logic;

import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract implementation of the {@link AlertHandler} interface.
 * <p>
 * This class has some common logic for some alert handlers. It takes care of
 * informing registered {@link AlertHandlerDoneListener}s. It also unregisters a
 * defined receiver if one has been registered.
 * </p>
 *
 * @author j
 */
public abstract class AbstractAlertHandler implements AlertHandler {

    private List<AlertHandlerDoneListener> listeners = new ArrayList<AlertHandlerDoneListener>(
            2);

    protected String telNr;

    protected BroadcastReceiver receiver;

    protected Context context;

    private boolean listenerNotified;

    public AbstractAlertHandler(String telNr) {
        this.telNr = telNr;
    }

    @Override
    public abstract void alert(Context context);

    /**
     * Method which is called when the listeners have been notified.
     * <p>
     * This means that the handler can finish up and close any needed receivers,
     * services, etc. This implementation unregisters any defined
     * {@link AbstractAlertHandler#receiver} from the
     * {@link AbstractAlertHandler#context}.
     * </p>
     * Override this method if needed.
     */
    private void listenersNotified() {
        // unregister the SmsBroadcastReveiver
        if (receiver != null && !listenerNotified) {
            context.unregisterReceiver(receiver);
            listenerNotified = true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see ch.jason.android.noiseMonitor.logic.AlertHandler#
     * registerAlertHandlerDoneListener
     * (ch.jason.android.noiseMonitor.logic.AlertHandlerDoneListener)
     */
    @Override
    public void registerListener(AlertHandlerDoneListener listener) {
        listeners.add(listener);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ch.jason.android.noiseMonitor.logic.AlertHandler#notifyListeners(int)
     */
    public void notifyListeners(int code) {
        for (AlertHandlerDoneListener listener : listeners) {
            listener.done(code);
        }

        // tell our handler that we're done notifying our registered listeners
        listenersNotified();
    }

}
