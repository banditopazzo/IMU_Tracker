package banditopazzo.imu_tracker.calibration.models;

public class OffsetsResults {

    private float[] accOffsets;
    private float[] gyroOffsets;

    public OffsetsResults(float[] accOffsets, float[] gyroOffsets) {
        this.accOffsets = accOffsets;
        this.gyroOffsets = gyroOffsets;
    }

    public float[] getAccOffsets() {
        return accOffsets;
    }

    public float[] getGyroOffsets() {
        return gyroOffsets;
    }
}
