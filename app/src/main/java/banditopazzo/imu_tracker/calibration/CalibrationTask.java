package banditopazzo.imu_tracker.calibration;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import banditopazzo.imu_tracker.calibration.models.CalibrationParams;
import banditopazzo.imu_tracker.calibration.models.OffsetsResults;
import banditopazzo.imu_tracker.calibration.sensors.OffsetListener;

public class CalibrationTask extends AsyncTask<CalibrationParams, Integer, OffsetsResults> {

    private CalibrationHandler CH;

    public CalibrationTask(CalibrationHandler CH) {
        this.CH = CH;
    }

    @Override
    protected OffsetsResults doInBackground(CalibrationParams... calibrationParams) {

        Looper.prepare();

        CalibrationParams params = calibrationParams[0];

        SensorManager SM = params.getSM();
        Handler handler = params.getHandler();

        //Get Accelerometer
        Sensor accelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Get Gyroscope
        Sensor gyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        OffsetListener accOffsetListener = new OffsetListener();
        OffsetListener gyroOffsetListener = new OffsetListener();

        SM.registerListener(accOffsetListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST, handler);
        SM.registerListener(gyroOffsetListener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST, handler);



        for (int i = 0; i < 5; i++) {
            try {
                publishProgress(5-i);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        SM.unregisterListener(accOffsetListener);
        SM.unregisterListener(gyroOffsetListener);

        float[] accOffsets = accOffsetListener.getFinalResults();
        float[] gyroOffsets = gyroOffsetListener.getFinalResults();

        float[] accSoglia = accOffsetListener.getMaxResults();
        float[] gyroSoglia = accOffsetListener.getMaxResults();

        return new OffsetsResults(accOffsets, gyroOffsets, accSoglia, gyroSoglia);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int value = values[0];
        CH.onCalibrationProgress(value);
    }

    @Override
    protected void onPostExecute(OffsetsResults results) {
        CH.onCalibration(results);
    }
}
