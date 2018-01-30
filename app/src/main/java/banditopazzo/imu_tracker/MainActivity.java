package banditopazzo.imu_tracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import banditopazzo.imu_tracker.calibration.CalibrationHandler;
import banditopazzo.imu_tracker.calibration.models.CalibrationParams;
import banditopazzo.imu_tracker.calibration.CalibrationTask;
import banditopazzo.imu_tracker.calibration.models.OffsetsResults;
import banditopazzo.imu_tracker.tracking.accelerometer.AccListener;
import banditopazzo.imu_tracker.tracking.gyroscope.GyroListener;
import banditopazzo.imu_tracker.tracking.imu_calculator.PositionFinderListener;
import banditopazzo.imu_tracker.tracking.trackingBoard.TrackingSurface;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CalibrationHandler {

    //Status
    private boolean running = false;

    //Logging
    private String TAG = "MAIN THREAD";

    //Views
    private TextView statusDisplay;
    /*
    0 - x,y
    1 - x,z
    2 - y,z
     */
    private TrackingSurface[] trackingSurfaces;

    //Sensors
    private SensorManager SM;
    private Sensor accelerometer, gyroscope;

    //Listeners
    private AccListener accListener;
    private GyroListener gyroListener;
    private PositionFinderListener positionFinderListener;

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

        //Divide screen in three sections
        LinearLayout ln = (LinearLayout) findViewById(R.id.container);
        LayoutParams surfaceDimension = new LayoutParams(LayoutParams.MATCH_PARENT,0,0.3f);
        LayoutParams lineDimension = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
        LinearLayout[] splitToThree = new LinearLayout[3];
        for (int i=0; i<3; i++) {
            splitToThree[i] = new LinearLayout(getApplicationContext());
            splitToThree[i].setLayoutParams(surfaceDimension);
            ln.addView(splitToThree[i]);
            //Add line between sections
            View v = new View(getApplicationContext());
            v.setLayoutParams(lineDimension);
            ln.addView(v);
        }

        //Add a tracking surface in each section
        trackingSurfaces = new TrackingSurface[3];
        for (int i=0; i<3; i++){
            trackingSurfaces[i] = new TrackingSurface(this);
            splitToThree[i].addView(trackingSurfaces[i]);
        }

        //Setup axis index
        List<Bitmap> scaledAxisIndexList = loadScaledAxisIndex();
        trackingSurfaces[0].setAxisIndex(scaledAxisIndexList.get(0));
        trackingSurfaces[1].setAxisIndex(scaledAxisIndexList.get(1));
        trackingSurfaces[2].setAxisIndex(scaledAxisIndexList.get(2));

        //Create sensor manager
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Get Accelerometer
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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
            SM.unregisterListener(accListener); //stoppare prima acc
            SM.unregisterListener(gyroListener);

            //Delete Listeners
            gyroListener=null;
            accListener=null;

            //Cancella il percorso
            for (TrackingSurface t: trackingSurfaces) {
                t.resetPosition();
            }

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
        SM.unregisterListener(positionFinderListener); //stoppare prima acc
        SM.unregisterListener(positionFinderListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: controllare il funzionamento
        SM.registerListener(positionFinderListener, gyroscope, SensorManager.SENSOR_DELAY_GAME, handler);
        SM.registerListener(positionFinderListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SM.unregisterListener(positionFinderListener); //stoppare prima acc
        SM.unregisterListener(positionFinderListener);
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
        accListener = new AccListener(trackingSurfaces, gyroListener);
        accListener.setOffsets(accOffsets);

        gyroListener.setAccelerationManager(accListener);

        positionFinderListener = new PositionFinderListener(trackingSurfaces);
        positionFinderListener.setGyroOffsets(gyroOffsets);
        positionFinderListener.setAccOffsets(accOffsets);

        //Start listeners
        SM.registerListener(positionFinderListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL, handler);
        SM.registerListener(positionFinderListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL,handler);

        //Update status and UI
        running = true;
        statusDisplay.setText("Running");

        Log.d(TAG,"Started Tracking");

    }

    @Override
    public void onCalibrationProgress(int value) {
        statusDisplay.setText("Calibration..." + value);
    }

    private List<Bitmap> loadScaledAxisIndex() {
        final double SCALE = 0.15;

        List<Bitmap> axisIndexList = new ArrayList<>();
        Bitmap xy = BitmapFactory.decodeResource(getResources(), R.drawable.xy);
        axisIndexList.add(xy);
        Bitmap xz = BitmapFactory.decodeResource(getResources(), R.drawable.xz);
        axisIndexList.add(xz);
        Bitmap yz = BitmapFactory.decodeResource(getResources(), R.drawable.yz);
        axisIndexList.add(yz);

        List<Bitmap> scaledAxisIndexList = new ArrayList<>();
        for (Bitmap b: axisIndexList) {
            Bitmap scaled_item = Bitmap.createScaledBitmap(
                    b,
                    (int) (b.getWidth() * SCALE),
                    (int) (b.getHeight() * SCALE),
                    true
            );
            scaledAxisIndexList.add(scaled_item);
        }

        return scaledAxisIndexList;
    }
}

