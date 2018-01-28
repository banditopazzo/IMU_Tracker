package banditopazzo.imu_tracker.tracking.imu_calculator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import banditopazzo.imu_tracker.tracking.accelerometer.UpgradableSurface;
import banditopazzo.imu_tracker.tracking.models.PointD;

import java.util.Date;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.tan;

public class PositionFinderListener implements SensorEventListener {

    //Status Matrix
    private double[][] statusMatrix;

    //Links to Entities
    private UpgradableSurface[] surfaces;

    //##################### Gyroscope Variables ######################################
    final float gyroSOGLIA = 0.02f;
    //last update time
    private double gyroT;
    //degrees array
    private double[] degrees;
    //offsets
    private float[] gyroOffsets;

    //##################### Accelerometer Variables ##################################
    final float accSOGLIA = 0.40f;
    //last update time
    private double accT;
    //offsets
    private float[] accOffsets;

    //Constructor
    public PositionFinderListener(UpgradableSurface[] surfaces) {

        //Initialize Status Matrix
        this.statusMatrix = new double[][]{
            {0,0,0},
            {0,0,0},
            {0,0,0}
        };

        //Set links to entities
        this.surfaces = surfaces;

        //########## Gyroscope Initialization ########################
        //Set the start time
        this.gyroT = new Date().getTime();
        //Set initial degrees to ZERO
        this.degrees = new double[]{0,0,0};
        //Set default offsets to ZERO
        this.gyroOffsets = new float[]{0,0,0};

        //########## Accelerometer Initialization ####################
        //Set the start time
        this.accT = new Date().getTime();
        //Set default offsets to ZERO
        this.accOffsets = new float[]{0,0,0};

    }

    public void setGyroOffsets(float[] offsets) {
        this.gyroOffsets = offsets;
    }

    public void setAccOffsets(float[] offsets) {
        this.accOffsets = offsets;
    }

    //Generic Callback
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
           // handleAccelerometer(sensorEvent);
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            handleGyroscope(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Accelerometer Callback
    private void handleAccelerometer(SensorEvent event) {

        //calculate dt update datetime
        long now = (new Date()).getTime();
        double dt = ((now - accT))/1000.000; // cast to double and conversion to seconds
        this.accT = now;

        //read acceleration
        float[] current_acc = {
                event.values[0] - accOffsets[0],
                -(event.values[1] - accOffsets[1]), //negativo perchè la Y viene disegnata al contrario sullo schermo
                event.values[2] - accOffsets[2]
        };

        //Check soglia
        for (int i = 0; i < current_acc.length; i++) {
            if (current_acc[i] < accSOGLIA) {
                current_acc[i] = 0;
            }
        }

        //Update State Matrix
        updateStateMatrix(
                current_acc,
                new float[]{0,0,0},
                dt
        );
        updateSurfaces();

    }

    //Gyroscope Callback
    private void handleGyroscope(SensorEvent event) {

        //calculate dt update datetime
        long now = (new Date()).getTime();
        double dt = ((now - gyroT))/1000.000; // cast to double and conversion to seconds
        this.gyroT = now;

        //Read values and subtract offset
        float[] velocities = {
                event.values[0] - gyroOffsets[0],
                event.values[1] - gyroOffsets[1],
                event.values[2] - gyroOffsets[2]
        };

        //Update degrees
        for (int i = 0; i < 3; i++) {
            //TODO: Test soglia: eliminare??
            if (Math.abs(velocities[i]) > gyroSOGLIA) {
                this.degrees[i] = this.degrees[i] + dt * velocities[i];
            }
        }

        //Update State Matrix
        updateStateMatrix(
                new float[]{0,0,0},
                velocities,
                dt
        );
        updateSurfaces();

    }

    private void updateStateMatrix(float[] accAcceleration, float[] gyroSpeed, double dt) {

        //TODO:filtro

        double[][] current = this.statusMatrix;

        double[][] change = new double[3][3];

        change[0][0] = gyroSpeed[0] + tan(current[0][1])*(gyroSpeed[1]*sin(current[0][0])+gyroSpeed[2]*cos(current[0][0]));
        change[0][1] = gyroSpeed[1]*cos(current[0][0])-gyroSpeed[2]*sin(current[0][0]);
        change[0][2] = sec(current[0][1])*(gyroSpeed[1]*sin(current[0][0])+gyroSpeed[2]*cos(current[0][0]));

        change[1][0] = accAcceleration[0]-gyroSpeed[1]*current[1][2]+gyroSpeed[2]*current[1][1];
        change[1][1] = accAcceleration[1]-gyroSpeed[2]*current[1][0]+gyroSpeed[0]*current[1][2];
        change[1][2] = accAcceleration[2]-gyroSpeed[0]*current[1][1]+gyroSpeed[1]*current[1][0];

        change[2][0] = current[1][0]*cos(current[0][1])*cos(current[0][2])+current[1][1]*(sin(current[0][0])*sin(current[0][1])*cos(current[0][2])-cos(current[0][0])*sin(current[0][2]))+current[1][2]*(cos(current[0][0])*sin(current[0][1])*cos(current[0][2])+sin(current[0][0])*sin(current[0][2]));
        change[2][1] = current[1][0]*cos(current[0][1])*sin(current[0][2])+current[1][1]*(sin(current[0][0])*sin(current[0][1])*sin(current[0][2])+cos(current[0][0])*sin(current[0][2]))+current[1][2]*(cos(current[0][0])*sin(current[0][1])*sin(current[0][2])-sin(current[0][0])*cos(current[0][2]));
        change[2][2] = -current[1][0]*sin(current[0][1])+current[1][1]*sin(current[0][0])*cos(current[0][1])+current[1][2]*cos(current[0][0])*cos(current[0][1]);

        this.statusMatrix = sumTwo3x3Matrices(current,scale3x3MatrixByNumber(change, dt));

    }

    private void updateSurfaces() {
        //Correct angles
        double[] correctDeg = {
                -this.degrees[2],
                this.degrees[1],
                -this.degrees[0]
        };
        surfaces[0].updateSurface(new PointD(statusMatrix[2][0],statusMatrix[2][1]), correctDeg[0]);
        surfaces[1].updateSurface(new PointD(statusMatrix[2][0],statusMatrix[2][2]), correctDeg[1]);
        surfaces[2].updateSurface(new PointD(-statusMatrix[2][1],statusMatrix[2][2]), correctDeg[2]);
    }
    
    private double[][] scale3x3MatrixByNumber(double[][] matrix, double scalar) {
        double[][] newMatrix = matrix.clone();
        for (int i = 0; i < newMatrix.length; i++) {
            for (int j = 0; j < newMatrix[i].length; j++) {
                newMatrix[i][j] = newMatrix[i][j] * scalar;
            }
        }
        return newMatrix;
    }

    private double[][] sumTwo3x3Matrices(double[][] one, double[][] other) {
        double[][] newMatrix = new double[][]{
                {0,0,0},
                {0,0,0},
                {0,0,0},
        };
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                newMatrix[i][j] = one[i][j] + other[i][j];
            }
        }
        return newMatrix;
    }

    private double sec(double angle) {
        return 1 / (cos(angle));
    }
}
