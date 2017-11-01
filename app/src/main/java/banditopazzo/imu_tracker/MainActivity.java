package banditopazzo.imu_tracker;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import banditopazzo.imu_tracker.calibration.CalibrationHandler;
import banditopazzo.imu_tracker.calibration.models.CalibrationParams;
import banditopazzo.imu_tracker.calibration.CalibrationTask;
import banditopazzo.imu_tracker.calibration.models.OffsetsResults;
import banditopazzo.imu_tracker.tracking.accelerometer.AccListener;
import banditopazzo.imu_tracker.tracking.gyroscope.GyroListener;
import banditopazzo.imu_tracker.tracking.trackingBoard.TrackingSurface;


public class MainActivity extends AppCompatActivity implements CalibrationHandler {

    //Status
    private boolean running = false;

    //Logging
    private String TAG = "MAIN THREAD";

    //Views
    private TextView statusDisplay;
    private TrackingSurface trackingSurface;

    //Sensors
    private SensorManager SM;
    private Sensor accelerometer, gyroscope;

    //Listeners
    private AccListener accListener;
    private GyroListener gyroListener;

    //Handler
    private HandlerThread handlerThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Display always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get status view
        statusDisplay = (TextView) findViewById(R.id.statusDisplay);

        //Set TrackingSurface View
        trackingSurface = new TrackingSurface(this);
        LinearLayout ln = (LinearLayout) findViewById(R.id.container);
        ln.addView(trackingSurface);

        //Create sensor manager
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Get Accelerometer
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Get Gyroscope
        gyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //Set up Handler
        this.handlerThread = new HandlerThread("SensorHandlerThread");
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());

        Log.d(TAG, "MainActivity Created");
    }

    public void startStopRecording(View v){
        if (!running) {

            //Setup Calibration Task
            CalibrationParams calParams = new CalibrationParams(SM,handler);
            CalibrationTask calibrationTask = new CalibrationTask(this);

            calibrationTask.execute(calParams);

            //Tracking starts asynchronously

        } else {

            //Stop Listeners
            SM.unregisterListener(gyroListener);
            SM.unregisterListener(accListener);

            //Delete Listeners
            gyroListener=null;
            accListener=null;

            //Cancella il percorso
            trackingSurface.resetPosition();

            //Update status and UI
            running = false;
            statusDisplay.setText("Not Running");

            Log.d(TAG,"Stopped Tracking");

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO: controllare il funzionamento
        SM.unregisterListener(gyroListener);
        SM.unregisterListener(accListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: controllare il funzionamento
        SM.registerListener(gyroListener, gyroscope, SensorManager.SENSOR_DELAY_GAME, handler);
        SM.registerListener(accListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Delete handler and stop handlerThread
        handler = null;
        handlerThread.quit(); //TODO: meglio quitSafely(), modificare l'API target
    }

    @Override
    public void onCalibration(OffsetsResults results) {

        //Offsets
        float[] accOffsets;
        float[] gyroOffsets;

        //Load offsets
        if (results != null) {
            accOffsets = results.getAccOffsets();
            gyroOffsets = results.getGyroOffsets();
        } else {
            accOffsets = new float[]{0,0,0};
            gyroOffsets = new float[]{0,0,0};
        }

        Log.d("OFFSET", "accOffsets: "  + accOffsets[0] + " " + accOffsets[1] + " " + accOffsets[2]);
        Log.d("OFFSET", "gyroOffsets: " + gyroOffsets[0] + " " + gyroOffsets[1] + " " + gyroOffsets[2]);


        //Set up listeners
        gyroListener = new GyroListener();
        gyroListener.setOffsets(gyroOffsets);
        accListener = new AccListener(trackingSurface, gyroListener);
        accListener.setOffsets(accOffsets);
        gyroListener.setAccelerationManager(accListener);

        //Start listeners
        SM.registerListener(gyroListener, gyroscope, SensorManager.SENSOR_DELAY_GAME, handler);
        SM.registerListener(accListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,handler);

        //Update status and UI
        running = true;
        statusDisplay.setText("Running");

        Log.d(TAG,"Started Tracking");

    }

    @Override
    public void onCalibrationProgress(int value) {
        statusDisplay.setText("Calibration..." + value);
    }
}

