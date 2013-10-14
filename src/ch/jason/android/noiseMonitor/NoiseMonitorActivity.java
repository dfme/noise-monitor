package ch.jason.android.noiseMonitor;

import android.os.Bundle;
import android.util.Log;
import com.phonegap.DroidGap;

/**
 * The Class NoiseMonitorActivity.
 * <p>
 * Just opens the browser and start our 'gui'
 * </p>
 *
 * @author j
 */
public class NoiseMonitorActivity extends DroidGap {

    private final String LOG_TAG = getClass().getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.main);
        Log.d(LOG_TAG, "Loading main.html");
        super.loadUrl("file:///android_asset/www/main.html");
    }
}