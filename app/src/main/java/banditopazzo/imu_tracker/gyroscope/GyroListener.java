package banditopazzo.imu_tracker.gyroscope;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import banditopazzo.imu_tracker.accelerometer.RotationManager;

import java.util.Date;

public class GyroListener implements RotationManager, SensorEventListener {

    //last update time
    private double t;
    private double gz;

    private double theta;

    public GyroListener() {

        //Set the start time
        this.t = new Date().getTime();

        theta=0.0f;
        gz=0;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //calculate dt update datetime
        long now = (new Date()).getTime();
        double dt = ((now - t))/1000.000; // cast to double and conversion to seconds
        this.t = now;

        //Read gz
        gz = event.values[2];

        final float SOGLIA = 0.02f;

        if (gz>SOGLIA)
            theta = theta + dt*gz;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public double getTheta() {
        return theta;
    }
}
