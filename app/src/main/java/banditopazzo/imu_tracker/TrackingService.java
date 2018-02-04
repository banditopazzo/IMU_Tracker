package banditopazzo.imu_tracker;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import banditopazzo.imu_tracker.tracking.imu_calculator.PositionFinderListener;

public class TrackingService extends Service {

    private final String TAG = "SERVICE_TAG";

    //Handler
    private HandlerThread handlerThread;
    private Handler handler;

    //Sensors
    private SensorManager SM;
    private Sensor accelerometer, gyroscope;

    //Listener
    private PositionFinderListener positionFinderListener;

    public TrackingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Set up Handler
        this.handlerThread = new HandlerThread("SensorHandlerThread");
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());

        //Create sensor manager
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Get Accelerometer
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //Get Gyroscope
        gyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Log.d(TAG, "Tracking Service Created");
    }

    @Override
    public int onStartCommand(Intent myIntent, int flags, int startId) {
        float[] accOffsets = new float[]{0,0,0};
        float[] gyroOffsets = new float[]{0,0,0};

        if (myIntent !=null && myIntent.getExtras()!=null) {
            accOffsets = myIntent.getExtras().getFloatArray("accOffsets");
            gyroOffsets = myIntent.getExtras().getFloatArray("gyroOffsets");
        }

        startTracking(accOffsets, gyroOffsets);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Delete handler and stop handlerThread
        handler = null;
        handlerThread.quit(); //TODO: meglio quitSafely(), modificare l'API target
        super.onDestroy();
    }

    private void startTracking(float[] accOffsets, float[] gyroOffsets) {
        //Create listeners
        positionFinderListener = new PositionFinderListener(trackingSurfaces);
        positionFinderListener.setGyroOffsets(gyroOffsets);
        positionFinderListener.setAccOffsets(accOffsets);

        //Start listeners
        SM.registerListener(positionFinderListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL, handler);
        SM.registerListener(positionFinderListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL,handler);
    }
}
