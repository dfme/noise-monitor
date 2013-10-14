package ch.jason.android.noiseMonitor.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import ch.jason.android.noiseMonitor.R;
import ch.jason.android.noiseMonitor.logic.SmsAlertHandler;

/**
 * The Class BatteryBroadcastReceiver.
 * <p>
 * Register this implementation of a {@link BroadcastReceiver} so that we can
 * inform the user of a low battery state. Register this class with
 * {@link Intent#ACTION_BATTERY_CHANGED} ({@link Intent#ACTION_BATTERY_LOW} does
 * not work!? needs to be tested!).
 * </p>
 */
public class BatteryBroadcastReceiver extends BroadcastReceiver {

    private final String LOG_TAG = getClass().getSimpleName();

    private String telNr;

    private static final int LOW_POWER = 5;

    private int lastReportedPowerLevel = -1;

    private boolean messageSent = false;

    /**
     * Instantiates a new battery broadcast receiver.
     *
     * @param telNr the tel nr
     */
    public BatteryBroadcastReceiver(String telNr) {
        this.telNr = telNr;
    }

    /**
     * This method checks if the device has enough power, If not a SMS will be
     * send.
     * <p>
     * If the device's battery state is
     * {@link BatteryManager#BATTERY_STATUS_DISCHARGING} and
     * {@link BatteryBroadcastReceiver#getLastReportedPowerLevel()} is below
     * {@link BatteryBroadcastReceiver#LOW_POWER} a SMS alert will be sent.
     * </p>
     *
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     *      android.content.Intent)
     */
    @Override
    public void onReceive(Context ctx, Intent intent) {

        int lastReportedBatteryState = intent.getIntExtra("status",
                BatteryManager.BATTERY_STATUS_UNKNOWN);
        lastReportedPowerLevel = intent.getIntExtra("level", 0);
        Log.d(LOG_TAG, "Battery state changed... Battery Level @ "
                + lastReportedPowerLevel + "% Battery State = "
                + lastReportedBatteryState);

        // only send message if the battery is really discharging and we have
        // not yet send an SMS
        if (!messageSent
                && lastReportedBatteryState == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            if (lastReportedPowerLevel <= LOW_POWER) {
                Log.i(LOG_TAG, "Battery state is low sending sms to : " + telNr);
                String message = ctx.getString(R.string.app_name)
                        + ": "
                        + ctx.getString(R.string.battery_low_sms_text,
                        lastReportedPowerLevel);
                SmsAlertHandler smsAlertHandler = new SmsAlertHandler(telNr,
                        message);
                smsAlertHandler.alert(ctx);
                messageSent = true;
            }
        }
    }

    /**
     * Gets the last reported power level.
     *
     * @return the last reported power level
     */
    public int getLastReportedPowerLevel() {
        return lastReportedPowerLevel;
    }
}
