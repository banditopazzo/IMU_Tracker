package banditopazzo.imu_tracker.SensorCommon;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import banditopazzo.imu_tracker.models.OffsetList3D;

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

        float x = results.getX_list().stream().reduce( (a,b) -> a+b ).get() / results.getX_list().size();

        float y = results.getY_list().stream().reduce( (a,b) -> a+b ).get() / results.getY_list().size();

        float z = results.getZ_list().stream().reduce( (a,b) -> a+b ).get() / results.getZ_list().size();

        float[] finalResults = {x,y,z};

        return finalResults;
    }
}
