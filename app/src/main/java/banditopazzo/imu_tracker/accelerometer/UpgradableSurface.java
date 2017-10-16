package banditopazzo.imu_tracker.accelerometer;

import banditopazzo.imu_tracker.models.PointD;

public interface UpgradableSurface {
    void updateSurface(PointD position);
}
