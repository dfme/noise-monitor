/**
 *
 */
package ch.jason.android.noiseMonitor;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import ch.jason.android.noiseMonitor.service.MonitorSoundService;

/**
 * @author j
 */
public class NoiseMonitorTestActivity extends Activity {

    private final String LOG_TAG = getClass().getSimpleName();

    private ProgressBar progressBar;

    private Handler handler = new Handler();

    private MediaRecorder mr;

    private volatile Integer amplitude;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_monitor_activity);

        // init mic
        try {
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mr.setOutputFile("/dev/null");
            mr.prepare();
            mr.start();

            progressBar = (ProgressBar) findViewById(R.id.progressBar1);
            progressBar.setProgress(1);

            // Start lengthy operation in a background thread
            new Thread(new Runnable() {

                public void run() {
                    try {
                        while (progressBar.getProgress() < 100) {
                            amplitude = ((mr.getMaxAmplitude() * 100) / MonitorSoundService.MAX_AMPLITUDE_FROM_MIC) + 1;
                            Log.d(LOG_TAG, "Read amplitude during test: "
                                    + amplitude);

                            // Update the progress bar
                            handler.post(new Runnable() {
                                public void run() {
                                    progressBar.setProgress(amplitude);
                                }
                            });
                            synchronized (amplitude) {
                                // wait(MonitorSoundService.DEFAULT_UPDATE_TIME);
                            }

                        }
                    } catch (Exception e) {
                        Log.i(LOG_TAG,
                                "Progress Bar Thread has been interrupted...", e);

                    }
                }
            }).start();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Errror while init Noise Monitor test view", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,
                "Activity is being destroyed, stopping microphone recording...");
        mr.stop();
        mr.reset();
        mr = null;
    }

}
