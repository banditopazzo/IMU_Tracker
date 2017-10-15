package banditopazzo.imu_tracker.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import banditopazzo.imu_tracker.models.PointD;
import banditopazzo.imu_tracker.trackingBoard.UpgradableSurface;

import java.util.Date;

public class AccListener implements SensorEventListener {

    //Links to Entities
    private UpgradableSurface surface;
    private RotationManager rm;

    //Logging
    private final String TAG = "SensorEventAccListener";

    //last update time
    private double t;

    //position
    private double xt;
    private double yt;

    //velocity
    private double vxt;
    private double vyt;


    public AccListener(UpgradableSurface surface, RotationManager rm) {

        //Set the start time
        this.t = new Date().getTime();

        //Set links to entities
        this.surface = surface;
        this.rm = rm;

        //Set velocity and position to ZERO
        this.xt = 0;
        this.yt = 0;
        this.vxt = 0;
        this.vyt = 0;

        Log.d(TAG, "Accelerometer Listener created");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Thread thread = Thread.currentThread();
        Log.d(TAG, "THD: " + thread.getName());

        //calculate dt update datetime
        long now = (new Date()).getTime();
        double dt = ((now - t))/1000.000; // cast to double and conversion to seconds
        this.t = now;

        //read acceleration
        float ax = -event.values[0];
        float ay = event.values[1];

        //process acceleration with data from gyroscope
        double theta = rm.getTheta();
        //TODO: calculate correct acceleration for x and y

        float SOGLIA = 0.25f;

        //TODO: provare a eliminare la soglia
        //Se non viene superata la soglia, considera nulla l'accelerazione
        if (Math.abs(ax)<SOGLIA) {
            ax=0;
        }
        if (Math.abs(ay)<SOGLIA){
            ay=0;
        }

        Log.d(TAG, "AX " + ax);
        Log.d(TAG, "AY " + ay);

        //update x position and velocity
        xt = 1 / 2 * ax * Math.pow(dt, 2) + vxt * dt + xt;
        vxt = ax * dt + vxt;

        //update y position and velocity
        yt = 1 / 2 * ay * Math.pow(dt, 2) + vyt * dt + yt;
        vyt = ay * dt + vyt;


        //Update Surface
        surface.updateSurface(new PointD(xt,yt));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Not used
    }
}

