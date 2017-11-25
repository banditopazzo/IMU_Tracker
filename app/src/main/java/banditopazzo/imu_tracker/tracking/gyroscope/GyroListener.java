package banditopazzo.imu_tracker.tracking.gyroscope;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import banditopazzo.imu_tracker.tracking.accelerometer.RotationManager;

import java.util.Date;

public class GyroListener implements RotationManager, SensorEventListener {

    final float SOGLIA = 0.02f;
    final float GRAVITY = 9.81f;

    //AccelerationManager
    private AccelerationManager am;

    //last update time
    private double t;

    //Degress
    private double xDegree;
    private double yDegree;
    private double zDegree;

    //offsets
    private float[] offsets;

    //Constructor
    public GyroListener() {

        //Set the start time
        this.t = new Date().getTime();

        //Set variables to zero
        xDegree=0.0f;
        yDegree=0.0f;

        zDegree=0.0f;
        offsets = new float[]{0,0,0};

    }

    public void setOffsets(float[] offsets) {
        this.offsets = offsets;
    }

    public void setAccelerationManager(AccelerationManager am) {
        this.am = am;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //calculate dt update datetime
        long now = (new Date()).getTime();
        double dt = ((now - t))/1000.000; // cast to double and conversion to seconds
        this.t = now;

        //Read values and subtract offset
        float gx = event.values[0] - offsets[0];
        float gy = event.values[1] - offsets[1];
        float gz = event.values[2] - offsets[2];

        //TODO: Test soglia: eliminare??
        if (Math.abs(gz)>SOGLIA) {
            xDegree = xDegree + dt * gx;
        }

        if (Math.abs(gz)>SOGLIA) {
            yDegree = yDegree + dt * gy;
        }

        if (Math.abs(gz)>SOGLIA) {
            zDegree = zDegree + dt * gz;
        }

        //TODO: synchronized su theta tutto questo blocco da qua fino alla fine
        //Metodo base


        //Complementary filter
        //TODO: switch ON/OFF complementary filter
        boolean attivo = false;
        //Esegui il complementary filter solo se è presente un AccelerationManager
        if (am != null && attivo) {

            //Ottieni forza totale dall'AccelerationManager
            float[] forces = am.getForces();

            //Se forces è nullo, non si può fare nulla
            if (forces == null)
                return;

            //Somma i valori assoluti delle forze
            float forceMagnitudeApprox = Math.abs(forces[0]) + Math.abs(forces[1]) + Math.abs(forces[2]);

            if (forceMagnitudeApprox > 0.5 * GRAVITY && forceMagnitudeApprox < 2 * GRAVITY) {

                //TODO: non sicuro sugli indici di forces
                double pitchAcc = Math.atan2(forces[1], forces[2]) * 180 / Math.PI;

                zDegree = zDegree * 0.98 + pitchAcc * 0.02;

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public double getX_Degree() {
        return -xDegree;
    }

    @Override
    public double getY_Degree() {
        return yDegree;
    }

    @Override
    public double getZ_Degree() {
        return -zDegree;
    }
}

