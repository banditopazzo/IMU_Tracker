package banditopazzo.imu_tracker.tracking.models;

import java.io.Serializable;

public class PointD implements Serializable {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public PointD(double x, double y) {

        this.x = x;
        this.y = y;
    }
}
