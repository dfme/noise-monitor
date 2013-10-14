/**
 *
 */
package ch.jason.android.noiseMonitor.logic;

import android.util.Log;
import ch.jason.android.noiseMonitor.object.AlertMode;

/**
 * This class creates an instance of {@link AlertHandler} depending on the
 * provided {@link AlertMode}.
 *
 * @author j
 */
public class AlertHandleFactory {

    private final String LOG_NAME = getClass().getSimpleName();

    public AlertHandler createHandlerAlert(AlertMode mode, String telNr) {

        switch (mode.getMode()) {
            case 0:
                return new TelephoneAlertHandler(telNr);
            case 1:
                return new SmsAlertHandler(telNr);
            default:
                Log.w(LOG_NAME, "Mode unknown or not supported: " + mode
                        + " Creating default handler: "
                        + TelephoneAlertHandler.class.getSimpleName());
                return new TelephoneAlertHandler(telNr);
        }
    }
}
