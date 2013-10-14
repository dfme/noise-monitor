/**
 *
 */
package ch.jason.android.noiseMonitor.phonegap.plugin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;
import ch.jason.android.noiseMonitor.R;
import ch.jason.android.noiseMonitor.logic.AlertHandler;
import ch.jason.android.noiseMonitor.service.MonitorSoundService;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
import org.json.JSONArray;

import java.util.Calendar;

/**
 * This class is used to plugin into Phonegap.
 * <p>
 * This class enables us to do more complex android code execution then the
 * Phonegap framework could provide. Our main goal here is to provide an
 * interface to JavaScript so that we can interact with Android Services (
 * {@link Service}) which monitor the device's microphone and initiates
 * {@link AlertHandler}s when the sound has exceeded a certain threshold.
 * </p>
 *
 * @author j
 */
public class NoiseMonitorPlugin extends Plugin {

    public static final String COMMAND_START = "start_monitor";
    public static final String COMMAND_TEST = "test_monitor";
    public static final String COMMAND_STOP = "stop_monitor";
    public static final String COMMAND_READ_MIC_AMPLITUDE = "mic_amplitude_max";
    public static final String COMMAND_IS_MONITOR_RUNNING = "is_monitor_running";
    public static final String COMMAND_VERSION = "app_version";
    private static MonitorSoundServiceConnection moServiceConnection = new MonitorSoundServiceConnection();
    private static boolean bound;
    private final String LOG_TAG = getClass().getSimpleName();

    /*
     * (non-Javadoc)
     *
     * @see com.phonegap.api.Plugin#execute(java.lang.String,
     * org.json.JSONArray, java.lang.String)
     */
    @Override
    public PluginResult execute(String action, JSONArray arguments,
                                String callBack) {
        Log.i(getClass().getSimpleName(),
                "Being called with following args: " + "action=" + action
                        + " JSON arguments=" + arguments.toString()
                        + " callbackId=" + callBack);
        if (COMMAND_START.equalsIgnoreCase(action))
            return startMonitor(arguments);
        else if (COMMAND_STOP.equalsIgnoreCase(action))
            return stopMonitor();
        else if (COMMAND_READ_MIC_AMPLITUDE.equalsIgnoreCase(action))
            return readMicMaxAmplitude();
        else if (COMMAND_IS_MONITOR_RUNNING.equalsIgnoreCase(action))
            return checkMonitorRunning();
        else if (COMMAND_VERSION.equalsIgnoreCase(action))
            return getVersion();
        else if (COMMAND_TEST.equalsIgnoreCase(action))
            return startMonitorTest(arguments);

        return new PluginResult(Status.INVALID_ACTION, "Invalid Action: "
                + action);
    }

    private PluginResult getVersion() {
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), 0);
            StringBuilder sb = new StringBuilder();
            sb.append("&copy;").append(Calendar.getInstance().get(Calendar.YEAR)).append(" ");
            sb.append(ctx.getString(R.string.app_name));
            sb.append(" v. ");
            sb.append(packageInfo.versionName);
            // sb.append(packageInfo.versionCode);
            return new PluginResult(Status.OK, sb.toString());
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "Error retrieving verison!", e);
            return new PluginResult(Status.ERROR, e.getLocalizedMessage());
        }
    }

    private PluginResult startMonitor(JSONArray args) {
        Log.i(getClass().getSimpleName(),
                "Trying to launch the monitor with following args: "
                        + args.toString());

        // first check if the phone capability
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getLine1Number() == null) {
            Log.e(LOG_TAG, "No phone line available! Aborting service start.");
            return new PluginResult(Status.ERROR,
                    ctx.getString(R.string.no_phone));
        }

        // bind arguments to our service connection
        moServiceConnection.setStartMonitorArguments(args);

        // first validate arguments
        if (!moServiceConnection.validArguments()) {
            return new PluginResult(Status.ERROR,
                    ctx.getString(R.string.invalid_arguments));
        }

        // now try to bind to our monitor service
        if (!bound) {
            Intent intent = new Intent(ctx, MonitorSoundService.class);
            try {
                ctx.startService(intent);
                if (ctx.bindService(intent, moServiceConnection,
                        Context.BIND_AUTO_CREATE)) {
                    bound = true;
                    String msg = ctx.getString(R.string.service_message);
                    // Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
                    return new PluginResult(Status.OK, msg);
                } else
                    return new PluginResult(Status.ERROR, "Could not bind "
                            + LOG_TAG);
            } catch (Exception ex) {
                return new PluginResult(Status.ERROR, "Could not bind "
                        + LOG_TAG + "due to an Exception: " + ex.getMessage());
            }
        } else {
            String msg = "Monitor already running!";
            Log.i(LOG_TAG, msg);
            return new PluginResult(Status.ILLEGAL_ACCESS_EXCEPTION, msg);
        }
    }

    private PluginResult stopMonitor() {
        if (bound) {
            Log.d(LOG_TAG, "Stopping the monitor...");

            int nrAlerts = moServiceConnection.getService().stopService();
            bound = false;
            ctx.unbindService(moServiceConnection);
            String userMsg = ctx.getString(R.string.service_message_stopped)
                    + " "
                    + ctx.getString(R.string.service_message_info, nrAlerts);
            return new PluginResult(Status.OK, userMsg);
        } else {
            Log.i(LOG_TAG, "Monitor Service not bound!");
            return new PluginResult(Status.ILLEGAL_ACCESS_EXCEPTION,
                    "Monitor not running!");
        }
    }

    private PluginResult readMicMaxAmplitude() {
        if (bound) {
            int amp = moServiceConnection.getService().readMicMaxAmplitude();
            Log.d(LOG_TAG, "Read max amplitude from mic: " + amp);
            if (amp >= 0)
                return new PluginResult(Status.OK, amp);
        }

        // if we get this far then either the service was not started or we
        // could not read from the mic
        return new PluginResult(Status.ERROR,
                "Microphone is not being monitored!");
    }

    private PluginResult checkMonitorRunning() {
        return new PluginResult(Status.OK,
                (moServiceConnection.getService() != null && bound));
    }

    private PluginResult startMonitorTest(JSONArray args) {
        // bind arguments to our service connection
        moServiceConnection.setStartMonitorArguments(args);

        // first validate arguments
        if (!moServiceConnection.validArguments()) {
            return new PluginResult(Status.ERROR,
                    ctx.getString(R.string.invalid_arguments));
        }

        // call our noise monitor test activity
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(ctx.getString(R.string.test_monitor_data_type));
        ctx.startActivity(intent);

        return new PluginResult(Status.OK, "Testing Noise Monitor...");
    }
}
