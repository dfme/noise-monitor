/**
 *
 */
package ch.jason.android.noiseMonitor.logic;

import android.content.*;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This class executes a telephone alert.
 * <p>
 * When {@link TelephoneAlertHandler#alert(android.content.Context)}} is called a
 * telephone call is placed for the given {@code telNr}. At the same time a
 * {@link BroadcastReceiver} is registered in the application context which
 * listens to any changes in the phone state line. As soon as the call
 * terminates ({@link TelephonyManager#CALL_STATE_IDLE}) any registered
 * listeners (which can be registered via
 * {@link TelephoneAlertHandler#registerListener(AlertHandlerDoneListener)}) are
 * notified.
 * </p>
 *
 * @author j
 */
public class TelephoneAlertHandler extends AbstractAlertHandler {

    private final String LOG_TAG = getClass().getSimpleName();

    /**
     * Instantiates a new telephone alert handler.
     *
     * @param telNr the tel nr
     */
    public TelephoneAlertHandler(String telNr) {
        super(telNr);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ch.jason.android.noiseMonitor.logic.IHandlerAlert#alert(android.app.Service
     * )
     */
    @Override
    public void alert(Context context) {

        this.context = context;

        // prepare our broadcast receiver to listen to the phone state
        receiver = new TelephoneServiceReceiver();
        IntentFilter intentFilter = new IntentFilter(
                TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        context.registerReceiver(receiver, intentFilter);

        // now place the call
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + telNr));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);

        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "Call failed", e);
        }
    }

    /**
     * This class handles phone change state events
     *
     * @author j
     */
    public class TelephoneServiceReceiver extends BroadcastReceiver {

        private final String LOG_TAG = getClass().getSimpleName();

        /*
         * (non-Javadoc)
         *
         * @see
         * android.content.BroadcastReceiver#onReceive(android.content.Context,
         * android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager telephony = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(new EndCallListener(),
                    PhoneStateListener.LISTEN_CALL_STATE);
        }

        private class EndCallListener extends PhoneStateListener {

            private boolean attemptedToCall;

            private boolean notified;

            private void notifyMonitorService() {
                if (!notified) {
                    notified = true;
                    Log.d(LOG_TAG, "Notifying Monitor Service...");
                    notifyListeners(AlertHandlerDoneListener.RESUME_CODE);
                } else {
                    Log.d(LOG_TAG, "Already notified Monitor Service...");
                }
            }

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (TelephonyManager.CALL_STATE_RINGING == state) {
                    Log.d(LOG_TAG, "Phone: RINGING, number: " + incomingNumber);
                }
                if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                    // wait for phone to go offhook (probably set a boolean
                    // flag) so
                    // you know your app initiated the call.
                    Log.d(LOG_TAG, "Phone: OFFHOOK");
                    attemptedToCall = true;
                }
                if (TelephonyManager.CALL_STATE_IDLE == state) {
                    // when this state occurs, and your flag is set, restart
                    // your app
                    Log.d(LOG_TAG, "Phone: IDLE");
                    if (attemptedToCall)
                        notifyMonitorService();
                }
            }
        }

    }
}
