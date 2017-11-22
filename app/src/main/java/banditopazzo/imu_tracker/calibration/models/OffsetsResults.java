package banditopazzo.imu_tracker.calibration.models;

public class OffsetsResults {

    private float[] accOffsets;
    private float[] gyroOffsets;
    private float[] accSoglia;
    private float[] gyroSoglia;

    public OffsetsResults(float[] accOffsets, float[] gyroOffsets, float[] accSoglia, float[] gyroSoglia) {
        this.accOffsets = accOffsets;
        this.gyroOffsets = gyroOffsets;
        this.accSoglia = accSoglia;
        this.gyroSoglia = gyroSoglia;

    }

    public float[] getAccOffsets() {
        return accOffsets;
    }

    public float[] getGyroOffsets() {
        return gyroOffsets;
    }

    public float[] getAccSoglia() {
        return accSoglia;
    }

    public float[] getGyroSoglia() {
        return gyroSoglia;
    }
}
