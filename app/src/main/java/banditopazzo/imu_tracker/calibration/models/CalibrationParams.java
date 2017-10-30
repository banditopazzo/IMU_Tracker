package banditopazzo.imu_tracker.calibration.models;

import android.hardware.SensorManager;

import android.os.Handler;

public class CalibrationParams {

    private SensorManager SM;
    private Handler handler;
    public CalibrationParams(SensorManager SM, Handler handler) {
        this.SM = SM;
        this.handler = handler;
    }

    public SensorManager getSM() {
        return SM;
    }

    public Handler getHandler() {
        return handler;
    }

}
