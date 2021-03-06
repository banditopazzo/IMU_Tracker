package banditopazzo.imu_tracker.tracking.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import banditopazzo.imu_tracker.tracking.accelerometer.models.GoldFishMemory;
import banditopazzo.imu_tracker.tracking.gyroscope.AccelerationManager;
import banditopazzo.imu_tracker.tracking.models.PointD;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Date;

public class AccListener implements SensorEventListener, AccelerationManager{

    private float[] soglia;

    //Links to Entities
    private UpgradableSurface[] surfaces;
    private RotationManager rm;

    //Logging
    private final String TAG = "SensorEventAccListener";

    //last update time
    private double t;

    //position
    private double xt;
    private double yt;
    private double zt;

    //velocity
    private double vxt;
    private double vyt;
    private double vzt;

    //Acceleration
    private double ax;
    private double ay;
    private double az;

    //Last N-Acceleration Values
    private GoldFishMemory<float[]> lastAccelerationValues;

    //Raw Acceleration Values
    private volatile float[] rawAcceleration;

    //offsets
    private float[] offsets;

    //Constructor
    public AccListener(UpgradableSurface[] surfaces, RotationManager rm) {

        //Set the start time
        this.t = new Date().getTime();

        //Set links to entities
        this.surfaces = surfaces;
        this.rm = rm;

        //Set velocity and position to ZERO
        this.xt = 0;
        this.yt = 0;
        this.zt = 0;
        this.vxt = 0;
        this.vyt = 0;
        this.vzt = 0;

        //Set acceleration to ZERO
        this.ax = 0;
        this.ay = 0;
        this.az = 0;

        //Set offsets to ZERO
        offsets = new float[]{0,0,0};

        //Set up the acceleration memory
        lastAccelerationValues = new GoldFishMemory<>(10);

        //Set up raw acceleration values
        rawAcceleration = new float[]{0,0,0};

        //Set soglia
        final float DEFAULT_SOGLIA = 0.40f;
        soglia = new float[]{DEFAULT_SOGLIA,DEFAULT_SOGLIA,DEFAULT_SOGLIA};

        Log.d(TAG, "Accelerometer Listener created");

    }

    public void setOffsets(float[] offsets) {
        this.offsets = offsets;
    }

    public void setSoglia(float[] soglia) {
        this.soglia = soglia;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //TODO: Usare timestamp
        //calculate dt and update datetime
        long now = (new Date()).getTime();
        double dt = (now - t) / 1000.000; // conversion to seconds and automatic conversion to double
        this.t = now;

        //read acceleration
        double current_ax = event.values[0] - offsets[0];
        double current_ay = -(event.values[1] - offsets[1]); //negativo perchè la Y viene disegnata al contrario sullo schermo
        double current_az = event.values[2] - offsets[2];

        //update raw acceleration -- per complementary filter
        //TODO: Valori compresi di offsets??
        rawAcceleration = new float[]{
                (float) current_ax,
                (float) current_ay,
                (float) current_az
        };

        //get rotation data from gyroscope
        double psy = -rm.getX_Degree();
        double theta = rm.getY_Degree();
        double phi = -rm.getZ_Degree();
        //Log.d(TAG, "Psy: " + psy);
        //Log.d(TAG, "Theta: " + theta);
        //Log.d(TAG, "Phi: " + phi);

        //process acceleration with rotation data
        //===================START ROTATION===============
        //Rotate Z
        double new_ax = (( current_ax * Math.cos(phi)) - (current_ay * Math.sin(phi)));
        current_ay = (( current_ax * Math.sin(phi)) + (current_ay * Math.cos(phi)));
        //Ignore Z ###############################################

        current_ax = new_ax;

        //Rotate Y
        new_ax = (( current_ax * Math.cos(theta)) + (current_az * Math.sin(theta)));
        //Ignore Y ###############################################
        current_az = (( current_ax * -Math.sin(theta)) + (current_az * Math.cos(theta)));

        current_ax = new_ax;

        //Rotate X
        //Ignore X ###############################################
        double new_ay = (( current_ay * Math.cos(psy)) - (current_az * Math.sin(psy)));
        current_az = (( current_ay * Math.sin(psy)) + (current_az * Math.cos(psy)));

        current_ay = new_ay;
        //====================END ROTATION================

        double rotated_ax = current_ax;
        double rotated_ay = current_ay;
        double rotated_az = current_az;

        //Se non viene superata la soglia, considera nulla l'accelerazione e la velocità - WTF ?!?
        if (Math.abs(rotated_ax)<soglia[0]) {
            rotated_ax=0;
            vxt=0;
        }
        if (Math.abs(rotated_ay)<soglia[1]) {
            rotated_ay=0;
            vyt=0;
        }
        if (Math.abs(rotated_az)<soglia[2]) {
            rotated_az=0;
            vzt=0;
        }

        //Filtro base
        ax = 0.98 * ax + 0.2 * (rotated_ax);
        ay = 0.98 * ay + 0.2 * (rotated_ay);
        az = 0.98 * az + 0.2 * (rotated_az);

        //Log processed acceleration
        //Log.d(TAG, "processed AX " + ax);
        //Log.d(TAG, "processed AY " + ay);
        //Log.d(TAG, "processed AY " + az);

        //update x position and velocity
        xt = 1 / 2 * ax * Math.pow(dt, 2) + vxt * dt + xt;
        vxt = ax * dt + vxt;

        //update y position and velocity
        yt = 1 / 2 * ay * Math.pow(dt, 2) + vyt * dt + yt;
        vyt = ay * dt + vyt;

        //update z position and velocity
        zt = 1 / 2 * az * Math.pow(dt, 2) + vzt * dt + zt;
        vzt = az * dt + vzt;

        //update acceleration memory
        //TODO: deve ricordare quelli rotati?? forse si...
        lastAccelerationValues.remember(new float[]{
                (float) rotated_ax,
                (float) rotated_ay,
                (float) rotated_az
        });

        //Update Surface
        surfaces[0].updateSurface(new PointD(xt,yt), phi);
        surfaces[1].updateSurface(new PointD(xt,zt), theta);
        surfaces[2].updateSurface(new PointD(-yt,zt), psy);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Not used
    }

    public float[] getForces() {
        return rawAcceleration;
    } //usato in complementary filter nel gyrolistener
}