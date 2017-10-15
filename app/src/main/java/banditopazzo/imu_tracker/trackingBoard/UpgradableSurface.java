package banditopazzo.imu_tracker.trackingBoard;

import banditopazzo.imu_tracker.models.PointD;

public interface UpgradableSurface {
    void updateSurface(PointD position);
}
