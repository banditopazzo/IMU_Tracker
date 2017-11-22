package banditopazzo.imu_tracker.calibration.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import banditopazzo.imu_tracker.calibration.sensors.models.OffsetList3D;

import java.util.List;

public class OffsetListener implements SensorEventListener {

    private OffsetList3D results;

    public OffsetListener() {
        results = new OffsetList3D();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        results.addBatch(event.values);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public float[] getFinalResults() {

        float x = getAverageFromFloatList(results.getX_list());

        float y = getAverageFromFloatList(results.getY_list());

        float z = getAverageFromFloatList(results.getZ_list());

        return new float[]{x,y,z};
    }

    public float[] getMaxResults() {

        float x = getMaxFromFloatList(results.getX_list());

        float y = getMaxFromFloatList(results.getY_list());

        float z = getMaxFromFloatList(results.getZ_list());

        return new float[]{x,y,z};

    }

    private float getAverageFromFloatList(List<Float> list) {
        float sum=0;
        for (float item : list) {
            sum = sum + item;
        }
        return sum/list.size();
    }

    private float getMaxFromFloatList(List<Float> list) {
        float max=0;
        for (float item : list) {
            if (Math.abs(item) > max)
                max = Math.abs(item);
        }
        return max;
    }

}
