/**
 *
 */
package ch.jason.android.noiseMonitor.phonegap.plugin;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import ch.jason.android.noiseMonitor.object.AlertMode;
import ch.jason.android.noiseMonitor.object.InvalidAlertModeException;
import ch.jason.android.noiseMonitor.service.MonitorSoundService;
import ch.jason.android.noiseMonitor.service.MonitorSoundService.MonitorBinder;
import org.json.JSONArray;

import java.util.regex.Pattern;

/**
 * This class implements Android's {@link ServiceConnection}.
 * <p>
 * This class is used to delegate the actual {@link MonitorSoundService} to the
 * binder (in our case {@link NoiseMonitorPlugin}.
 * </p>
 *
 * @author j
 */
public class MonitorSoundServiceConnection implements ServiceConnection {

    private final String LOG_TAG = getClass().getSimpleName();

    private MonitorSoundService service;

    private JSONArray startMonitorArguments;

    public MonitorSoundService getService() {
        return service;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.content.ServiceConnection#onServiceConnected(android.content.
     * ComponentName, android.os.IBinder)
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((MonitorBinder) binder).getMonitorService();
        try {
            service.setTelephoneNr(startMonitorArguments.getString(0));
            service.setThreshold(startMonitorArguments.getInt(1));
            service.setAlertMode(AlertMode.parseAlertMode(startMonitorArguments
                    .getInt(2)));
            service.setBabyPhoneMode("on"
                    .equalsIgnoreCase(startMonitorArguments.getString(3)));
            service.startService();
            Log.i(LOG_TAG, "Service has started and has been bound.");
        } catch (Exception ex) {
            Log.e(LOG_TAG, "An Exception was thrown during service init!", ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.content.ServiceConnection#onServiceDisconnected(android.content
     * .ComponentName)
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(LOG_TAG, "Service has been unbound and will terminate...");
        service.stopService();
        service = null;
    }

    /**
     * Sets the start monitor arguments.
     *
     * @param startMonitorArguments needed to init the {@link MonitorSoundService}
     */
    public void setStartMonitorArguments(JSONArray startMonitorArguments) {
        this.startMonitorArguments = startMonitorArguments;
    }

    /**
     * Validates the {@link MonitorSoundServiceConnection#startMonitorArguments}
     * arguments.
     *
     * @return true, if arguments are valid, false otherwise
     */
    public boolean validArguments() {
        try {
            if (startMonitorArguments == null) {
                return false;
            }

            // check telephone number
            String telNr = startMonitorArguments.getString(0);
            if (telNr == null || "".equalsIgnoreCase(telNr)
                    || !Pattern.matches("(\\+)?\\d*", telNr)) {
                Log.d(LOG_TAG, "telNr invalid: " + telNr);
                return false;
            }

            // check threshold
            int threshold = startMonitorArguments.getInt(1);
            if (threshold <= 0) {
                Log.d(LOG_TAG, "telNr invalid: " + telNr);
                return false;
            }

            // check alert mode (if the mode is unknown an
            // InvalidAlertModeException will be thrown
            int alertMode = startMonitorArguments.getInt(2);
            try {
                AlertMode.parseAlertMode(alertMode);
            } catch (InvalidAlertModeException ex) {
                Log.d(LOG_TAG, "babyphoneValue invalid: " + alertMode);
            }

            // check babyphone mode
            String babyphoneValue = startMonitorArguments.getString(3);
            if (babyphoneValue == null
                    || !Pattern.matches("(on)?|(off)?", babyphoneValue)) {
                Log.d(LOG_TAG, "babyphoneValue invalid: " + babyphoneValue);
                return false;
            }

        } catch (Exception e) {
            Log.w(LOG_TAG, "Exception occurred during arguments validation!", e);
            return false;
        }

        // if we get this far everything is ok
        return true;
    }

}
