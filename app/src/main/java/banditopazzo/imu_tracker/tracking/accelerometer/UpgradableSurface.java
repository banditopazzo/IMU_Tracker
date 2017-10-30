package banditopazzo.imu_tracker.tracking.accelerometer;

import banditopazzo.imu_tracker.tracking.models.PointD;

public interface UpgradableSurface {
    void updateSurface(PointD position);
}
