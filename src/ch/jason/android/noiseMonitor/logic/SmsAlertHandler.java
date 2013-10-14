/**
 *
 */
package ch.jason.android.noiseMonitor.logic;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import ch.jason.android.noiseMonitor.R;

/**
 * This class executes a SMS alert.
 * <p>
 * When {@link SmsAlertHandler#alert(android.content.Context)}} is called a sms text
 * is sent to the given {@code telNr}. At the same time a
 * {@link BroadcastReceiver} is registered in the application context which
 * listens to when the sms has been sent. As soon as the text has been sent any
 * registered listeners (which can be registered via
 * {@link TelephoneAlertHandler#registerListener(AlertHandlerDoneListener)}) are
 * notified with {@link AlertHandlerDoneListener#STOP_CODE} if the text was sent
 * successfully or with {@link AlertHandlerDoneListener#RESUME_CODE} if sending
 * the sms message failed.
 * </p>
 *
 * @author j
 */
public class SmsAlertHandler extends AbstractAlertHandler {

    private static final String INTENT_ACTION_SMS_SENT = "SMS_SENT";

    private final String LOG_TAG = getClass().getSimpleName();

    private String message = null;

    /**
     * Instantiates a new sms alert handler.
     *
     * @param telNr the tel nr
     */
    public SmsAlertHandler(String telNr) {
        super(telNr);
    }

    /**
     * Instantiates a new sms alert handler.
     *
     * @param telNr   the tel nr
     * @param message the text message to send
     */
    public SmsAlertHandler(String telNr, String message) {
        super(telNr);
        this.message = message;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ch.jason.android.noiseMonitor.logic.AbstractAlertHandler#alert(ch.jason
     * .android.noiseMonitor.service.MonitorSoundService)
     */
    @Override
    public void alert(Context context) {
        this.context = context;

        // prepare message
        if (message == null)
            message = "Hello, noise has been detected by '"
                    + context.getString(R.string.app_name) + "'";

        // prepare our broadcast receiver to listen to the message sent state
        receiver = new SmsBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(INTENT_ACTION_SMS_SENT);
        context.registerReceiver(receiver, intentFilter);

        // send an sms with a pending intent which triggers our receiver
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(INTENT_ACTION_SMS_SENT), 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(telNr, null, message, sentPI, null);

        Log.d(LOG_TAG, "An SMS alert has been sent to: " + telNr
                + " with message: " + message);

    }

    /**
     * The Class SmsBroadcastReceiver.
     * <p>
     * This receiver is registered to receive any incoming intents which
     * determine if our sms was sent successfully.
     * </p>
     */
    private class SmsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = getResultCode();
            String message;
            if (Activity.RESULT_OK == resultCode) {
                message = "SMS successfully sent to " + telNr;
                Log.i(LOG_TAG, message);
                notifyListeners(AlertHandlerDoneListener.STOP_CODE);
            } else {
                message = "Error while sending an SMS message to " + telNr
                        + " . Result code = " + resultCode;
                Log.w(LOG_TAG, message);
                notifyListeners(AlertHandlerDoneListener.RESUME_CODE);
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

    }

}
