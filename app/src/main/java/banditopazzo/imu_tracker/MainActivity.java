package banditopazzo.imu_tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import banditopazzo.imu_tracker.trackingBoard.TrackingSurface;


public class MainActivity extends AppCompatActivity {

    //Status
    private boolean running = false;

    //Logging
    private String TAG = "SensorEvent";

    //Views
    private TextView statusDisplay;
    private TrackingSurface ts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get status view
        statusDisplay = (TextView) findViewById(R.id.statusDisplay);

        //Set TrackingSurface View
        ts = new TrackingSurface(this);
        LinearLayout ln = (LinearLayout) findViewById(R.id.container);
        ln.addView(ts);

        Log.d(TAG, "MainActivity Created");
    }

    public void startStopRecording(View v){
        if (!running) {
            //Start tracking on the surface
            ts.start();
            //Update status and UI
            running = true;
            statusDisplay.setText("Running");
            Log.d(TAG,"Started Tracking");
        } else {
            ts.stop();
            //Update status and UI
            running = false;
            statusDisplay.setText("Not Running");
            Log.d(TAG,"Stopped Tracking");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ts.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ts.resume();
    }

}

//TODO: forse si puo passare il riferimento ad oggetto PuntoD da mainActivity ad AccListener e non serve richiamare updateUI, verificare...

