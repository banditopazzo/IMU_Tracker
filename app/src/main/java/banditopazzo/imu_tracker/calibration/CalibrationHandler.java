package banditopazzo.imu_tracker.calibration;

import banditopazzo.imu_tracker.calibration.models.OffsetsResults;

public interface CalibrationHandler {

    void onCalibrationProgress(int value);

    void onCalibration(OffsetsResults results);
}
