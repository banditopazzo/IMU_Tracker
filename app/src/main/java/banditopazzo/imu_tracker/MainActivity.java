package banditopazzo.imu_tracker;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import banditopazzo.imu_tracker.accelerometer.AccListener;
import banditopazzo.imu_tracker.gyroscope.GyroListener;
import banditopazzo.imu_tracker.trackingBoard.TrackingSurface;


public class MainActivity extends AppCompatActivity {

    //Status
    private boolean running = false;

    //Logging
    private String TAG = "SensorEvent";

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

            //Start tracking on the surface - Start listeners
            gyroListener = new GyroListener();
            SM.registerListener(gyroListener, gyroscope, SensorManager.SENSOR_DELAY_GAME, handler);
            accListener = new AccListener(trackingSurface, gyroListener);
            SM.registerListener(accListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,handler);

            //Update status and UI
            running = true;
            statusDisplay.setText("Running");

            Log.d(TAG,"Started Tracking");

        } else {

            //Stop Listeners
            SM.unregisterListener(gyroListener);
            SM.unregisterListener(accListener);

            //Delete Listeners
            gyroListener=null;
            accListener=null;

            //Delete handler and stop handlerThread
            handler = null;
            handlerThread.quit(); //TODO: meglio quitSafely(), modificare l'API target

            //TODO: cancella il percorso

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

}

//TODO: forse si puo passare il riferimento ad oggetto PuntoD da mainActivity ad AccListener e non serve richiamare updateUI, verificare...

