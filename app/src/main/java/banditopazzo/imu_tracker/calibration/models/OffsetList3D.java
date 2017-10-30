package banditopazzo.imu_tracker.calibration.models;

import java.util.LinkedList;
import java.util.List;

public class OffsetList3D {

    private List<Float> x_list;
    private List<Float> y_list;
    private List<Float> z_list;

    public OffsetList3D() {
        x_list = new LinkedList<>();
        y_list = new LinkedList<>();
        z_list = new LinkedList<>();
    }

    public List<Float> getX_list() {
        return x_list;
    }

    public List<Float> getY_list() {
        return y_list;
    }

    public List<Float> getZ_list() {
        return z_list;
    }

    public void addBatch(float[] batch) {
        x_list.add(batch[0]);
        y_list.add(batch[1]);
        z_list.add(batch[2]);
    }
}
