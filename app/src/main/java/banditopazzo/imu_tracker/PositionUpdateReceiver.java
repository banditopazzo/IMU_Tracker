package banditopazzo.imu_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import banditopazzo.imu_tracker.tracking.models.UpdateInfo;
import banditopazzo.imu_tracker.tracking.trackingBoard.TrackingSurface;

public class PositionUpdateReceiver extends BroadcastReceiver {

    private TrackingSurface[] trackingSurfaces;

    public PositionUpdateReceiver(TrackingSurface[] trackingSurfaces) {
        this.trackingSurfaces = trackingSurfaces;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateInfo data = (UpdateInfo) intent.getExtras().get("data");
        trackingSurfaces[0].updateSurface(data.getFirstPoint(), data.getFirstAngle());
        trackingSurfaces[1].updateSurface(data.getSecondPoint(), data.getSecondAngle());
        trackingSurfaces[2].updateSurface(data.getThirdPoint(), data.getThirdAngle());
    }

}
