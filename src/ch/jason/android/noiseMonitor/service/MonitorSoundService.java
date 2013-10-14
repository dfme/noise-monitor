/**
 *
 */
package ch.jason.android.noiseMonitor.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import ch.jason.android.noiseMonitor.NoiseMonitorActivity;
import ch.jason.android.noiseMonitor.R;
import ch.jason.android.noiseMonitor.logic.AlertHandleFactory;
import ch.jason.android.noiseMonitor.logic.AlertHandler;
import ch.jason.android.noiseMonitor.logic.AlertHandlerDoneListener;
import ch.jason.android.noiseMonitor.object.AlertMode;

import java.util.UUID;

/**
 * This class is used to monitor any sound coming from the device's microphone.
 * <p>
 * When the sound has exceeded a given {@code threshold} a {@link AlertHandler}
 * is informed which is instantiated over our {@link AlertHandleFactory}.
 * </p>
 *
 * @author j
 */
public class MonitorSoundService extends Service {

    /**
     * Default amplitude read time from the mic in ms
     */
    public static final int DEFAULT_UPDATE_TIME = 250;
    /**
     * Max amplitude that can be read from the mic
     */
    public static final int MAX_AMPLITUDE_FROM_MIC = 32768;
    /**
     * Default time in ms to wait after an alert has been sent before alerting again
     */
    private static final int DEFAULT_WAIT_TIME = 5000;
    private final String LOG_NAME = getClass().getSimpleName();
    /* Notification Stuff */
    private final int NOTIFICATION_ID = 101;
    /**
     * Used to return this service over the {@link Binder} interface.
     */
    private MonitorBinder binder = new MonitorBinder();
    /**
     * Used to read from the microphone
     */
    private MediaRecorder mr;
    /* User Settings */
    private String telephoneNr;
    private int threshold;
    private AlertMode alertMode;
    private boolean babyPhoneMode;
    /**
     * The telephone's current ringer mode
     */
    private int currentRingerMode;
    private int updateTime = DEFAULT_UPDATE_TIME;
    /**
     * Our work horse
     */
    private MonitorThread monitorThread;
    /**
     * Handlers which execute the alerts
     */
    private AlertHandleFactory alertHandleFactory = new AlertHandleFactory();
    /**
     * Our battery monitor
     */
    private BatteryBroadcastReceiver batteryReceiver;
    private Notification notification;
    private PendingIntent pendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Start service.
     */
    public void startService() {
        // place notification in the status bar
        notification = new Notification(R.drawable.ic_stat_noisemonitor,
                getString(R.string.service_started,
                        getString(R.string.app_name)),
                System.currentTimeMillis());

        Intent notificationIntent = new Intent(this, NoiseMonitorActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                Intent.FLAG_ACTIVITY_CLEAR_TOP);

        notification
                .setLatestEventInfo(
                        this,
                        getString(R.string.service_title,
                                getString(R.string.app_name)),
                        getText(R.string.service_message), pendingIntent);
        startForeground(NOTIFICATION_ID, notification);

        // turn off ringer and vibrator if we're in babyphone mode
        if (babyPhoneMode) {
            Log.d(LOG_NAME, "BabyPhoneMode was selected. "
                    + "Setting the phone's ringer mode to SILENT");
            AudioManager am = (AudioManager) getApplicationContext()
                    .getSystemService(Context.AUDIO_SERVICE);
            currentRingerMode = am.getRingerMode();
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }

        // start recording from mic
        try {
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mr.setOutputFile("/dev/null");
            mr.prepare();
            mr.start();
            // poll the mic amplitude read using a thread
            monitorThread = new MonitorThread(String.valueOf(System
                    .currentTimeMillis()));
            monitorThread.startThread();
        } catch (Exception ex) {
            Log.e(LOG_NAME, "Could not start recording from mic!", ex);
            stopService();
        }

        // register our battery monitor receiver
        batteryReceiver = new BatteryBroadcastReceiver(telephoneNr);
        IntentFilter batteryLevelFilter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED);
        getApplicationContext().registerReceiver(batteryReceiver,
                batteryLevelFilter);

        Log.i(LOG_NAME, "Monitoring the mic...");
    }

    /**
     * Stop service.
     *
     * @return the number of alerts that have been registered during a session.
     */
    public int stopService() {
        // stop monitor thread
        monitorThread.stopThread();

        // set the previous ringer mode if needed
        if (babyPhoneMode) {
            Log.d(LOG_NAME,
                    "BabyPhoneMode was selected. "
                            + "Setting the phone's ringer mode back to it's current state before monitoring: "
                            + currentRingerMode);
            AudioManager am = (AudioManager) getApplicationContext()
                    .getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(currentRingerMode);
        }

        // shutoff mic
        mr.stop();
        mr.reset();
        mr.release();
        mr = null;

        // stop our battery monitor
        getApplicationContext().unregisterReceiver(batteryReceiver);

        // stop notification
        stopForeground(true);

        int nrAlerts = monitorThread.getNrExceededThresholds();
        Log.i(LOG_NAME,
                "Stopped monitoring the mic. Threshold has been exceeded "
                        + nrAlerts + " times.");
        return nrAlerts;
    }

    private void executeAlert() {
        monitorThread.pauseMonitor();
        AlertHandler alertHandler = alertHandleFactory.createHandlerAlert(
                alertMode, telephoneNr);
        alertHandler.registerListener(new AlertHandlerDoneListener() {

            @Override
            public void done(int code) {
                Log.d(LOG_NAME, "Alert is done...");
                if (AlertHandlerDoneListener.STOP_CODE == code) {
                    // monitorThread.stopThread();
                    // stopService();
                    updateNotification(getString(R.string.service_message_stopped));
                } else {
                    updateNotification(getString(R.string.service_message));
                    monitorThread.resumeMonitor();
                }
            }
        });

        alertHandler.alert(this);
    }

    private void updateNotification(String serviceMessage) {

        notification
                .setLatestEventInfo(
                        this,
                        getString(R.string.service_title,
                                getString(R.string.app_name)),
                        serviceMessage
                                + "\n"
                                + getString(R.string.service_message_info,
                                monitorThread.getNrExceededThresholds()),
                        pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Reads the max amplitude from the mic.
     *
     * @return the int
     */
    public int readMicMaxAmplitude() {
        return mr.getMaxAmplitude();
        // return batteryReceiver.getLastReportedPowerLevel();
    }

    /**
     * Sets the telephone nr.
     *
     * @param telephoneNr the new telephone nr
     */
    public void setTelephoneNr(String telephoneNr) {
        this.telephoneNr = telephoneNr;
    }

    /**
     * The threshold is set as follows:
     * <p/>
     * <pre>
     * (Math.pow(1.75, threshold) * 100) + (200/threshold)
     * </pre>
     *
     * @param threshold the threshold to set
     */
    public void setThreshold(int threshold) {
        this.threshold = (int) ((Math.pow(1.75, threshold) * 100) + (200 / threshold));
    }

    /**
     * Sets the alert mode.
     *
     * @param alertMode the new alert mode
     */
    public void setAlertMode(AlertMode alertMode) {
        this.alertMode = alertMode;
    }

    /**
     * Sets the baby phone mode.
     *
     * @param babyPhoneMode the new baby phone mode
     */
    public void setBabyPhoneMode(boolean babyPhoneMode) {
        this.babyPhoneMode = babyPhoneMode;
    }

    /**
     * The Class MonitorBinder which returns the {@link MonitorSoundService}.
     */
    public class MonitorBinder extends Binder {

        public MonitorSoundService getMonitorService() {
            return MonitorSoundService.this;
        }
    }

    /**
     * The Class MonitorThread.
     * <p>
     * This class monitors the microphone and executes appropriate alert when
     * the {@link MonitorSoundService#threshold} has been exceeded.
     * </p>
     */
    private class MonitorThread extends Thread {

        private String LOG_TAG = "[" + UUID.randomUUID() + "]";
        private volatile Thread runner;
        private volatile Boolean threadSuspended = Boolean.FALSE;
        private int nrExceededThresholds;

        public MonitorThread(String name) {
            super(name);
        }

        @Override
        public void run() {

            try {
                boolean justAlerted = false;
                int maxAmplitude;
                while (true) {
                    synchronized (this) {
                        while (threadSuspended) {
                            Log.d(LOG_TAG, "Pausing mic monitor...");
                            wait();
                            Log.d(LOG_TAG, "Resuming mic monitor...");
                        }
                    }
                    maxAmplitude = readMicMaxAmplitude();
                    if (maxAmplitude <= threshold) {
                        Log.d(LOG_TAG, "Amplitude (" + maxAmplitude
                                + ") is below threshold (" + threshold + ")");
                        sleep(updateTime);
                        justAlerted = false;
                    } else {
                        Log.i(LOG_TAG, "Threshold (" + threshold
                                + ") has been exceeded with " + maxAmplitude);
                        if (justAlerted) {
                            sleep(DEFAULT_WAIT_TIME);
                            justAlerted = false;
                        } else {
                            nrExceededThresholds++;
                            executeAlert();
                            justAlerted = true;
                        }
                    }

                }
            } catch (InterruptedException ex) {
                Log.i(LOG_TAG, "Monitor Thread has been interrupted!");
            }

        }

        /**
         * Gets the nr of exceeded thresholds.
         *
         * @return the nr exceeded thresholds
         */
        public int getNrExceededThresholds() {
            return nrExceededThresholds;
        }

        /**
         * Start thread. Use this method instead of {@link Thread#start()}.
         */
        public synchronized void startThread() {
            if (runner == null) {
                runner = new Thread(this, "Sound Monitor Thread");
                runner.start();
            }
        }

        /**
         * Stop thread. Use this method instead of {@link Thread#stop()} and
         * {@link Thread#interrupt()}.
         */
        public synchronized void stopThread() {
            if (runner != null) {
                Thread moribund = runner;
                runner = null;
                moribund.interrupt();
            }
        }

        /**
         * Pause Monitor. Use this method instead of {@link Thread#stop()} and
         */
        public synchronized void pauseMonitor() {
            if (runner != null) {
                threadSuspended = true;
            }
        }

        public synchronized void resumeMonitor() {
            threadSuspended = false;

            synchronized (this) {
                notify();
            }
        }
    }
}
