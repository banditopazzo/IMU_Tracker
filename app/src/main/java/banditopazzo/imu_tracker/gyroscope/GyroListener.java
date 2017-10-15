package banditopazzo.imu_tracker.gyroscope;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import banditopazzo.imu_tracker.accelerometer.RotationManager;

public class GyroListener implements RotationManager, SensorEventListener {

    private double theta=0.0f;

    public GyroListener() {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public double getTheta() {
        return theta;
    }
}
