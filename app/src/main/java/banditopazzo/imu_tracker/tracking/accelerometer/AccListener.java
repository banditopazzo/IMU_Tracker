package banditopazzo.imu_tracker.tracking.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import banditopazzo.imu_tracker.tracking.accelerometer.models.GoldFishMemory;
import banditopazzo.imu_tracker.tracking.gyroscope.AccelerationManager;
import banditopazzo.imu_tracker.tracking.models.PointD;

import java.util.Date;

public class AccListener implements SensorEventListener, AccelerationManager{

    private final float SOGLIA = 0.30f;

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

    //Acceleration
    private double ax;
    private double ay;

    //Last N-Acceleration Values
    private GoldFishMemory<float[]> lastAccelerationValues;

    //Raw Acceleration Values
    private volatile float[] rawAcceleration;

    //offsets
    private float[] offsets;

    //Constructor
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

        //Set acceleration to ZERO
        this.ax = 0;
        this.ay = 0;

        //Set offsets to ZERO
        offsets = new float[]{0,0,0};

        //Set up the acceleration memory
        lastAccelerationValues = new GoldFishMemory<>(10);

        //Set up raw acceleration values
        rawAcceleration = new float[]{0,0,0};

        Log.d(TAG, "Accelerometer Listener created");

    }

    public void setOffsets(float[] offsets) {
        this.offsets = offsets;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //calculate dt and update datetime
        long now = (new Date()).getTime();
        double dt = (now - t) / 1000.000; // conversion to seconds and automatic conversion to double
        this.t = now;

        //read acceleration
        double current_ax = event.values[0] - offsets[0];
        double current_ay = -event.values[1] - offsets[1];
        double current_az = event.values[2] - offsets[2];

        //update raw acceleration
        //TODO: Valori comprensivi solo di offsets
        rawAcceleration = new float[]{
                (float) current_ax,
                (float) current_ay,
                (float) current_az
        };

        //process acceleration with data from gyroscope
        double theta = rm.getTheta();
        current_ax = Math.cos(theta)*current_ax - Math.sin(theta)*current_ay;
        current_ay = Math.sin(theta)*current_ax + Math.cos(theta)*current_ay;
        Log.d(TAG, "Theta: " + theta);

        //Se non viene superata la soglia, considera nulla l'accelerazione e la velocità
        if (Math.abs(current_ax)<SOGLIA) {
            current_ax=0;
            vxt=0;
        }
        if (Math.abs(current_ay)<SOGLIA){
            current_ay=0;
            vyt=0;
        }

        //Filtro base
        ax = 0.9 * ax + 0.1 * (current_ax);
        ay = 0.9 * ay + 0.1 * (current_ay);

        //Log processed acceleration
        Log.d(TAG, "AX " + ax);
        Log.d(TAG, "AY " + ay);

        //update x position and velocity
        xt = 1 / 2 * ax * Math.pow(dt, 2) + vxt * dt + xt;
        vxt = ax * dt + vxt;

        //update y position and velocity
        yt = 1 / 2 * ay * Math.pow(dt, 2) + vyt * dt + yt;
        vyt = ay * dt + vyt;

        //update acceleration memory
        lastAccelerationValues.remember(new float[]{
                (float) current_ax,
                (float) current_ay,
                (float) current_az
        });

        //Update Surface
        surface.updateSurface(new PointD(xt,yt), theta);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Not used
    }

    public float[] getForces() {
        return rawAcceleration;
    }
}

