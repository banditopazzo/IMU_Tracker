package banditopazzo.imu_tracker.tracking.models;

import java.io.Serializable;

public class UpdateInfo implements Serializable {

    private PointD firstPoint;
    private double firstAngle;

    private PointD secondPoint;
    private double secondAngle;

    private PointD thirdPoint;
    private double thirdAngle;

    public UpdateInfo(PointD firstPoint, double firstAngle, PointD secondPoint, double secondAngle, PointD thirdPoint, double thirdAngle) {
        this.firstPoint = firstPoint;
        this.firstAngle = firstAngle;
        this.secondPoint = secondPoint;
        this.secondAngle = secondAngle;
        this.thirdPoint = thirdPoint;
        this.thirdAngle = thirdAngle;
    }

    public PointD getFirstPoint() {
        return firstPoint;
    }

    public double getFirstAngle() {
        return firstAngle;
    }

    public PointD getSecondPoint() {
        return secondPoint;
    }

    public double getSecondAngle() {
        return secondAngle;
    }

    public PointD getThirdPoint() {
        return thirdPoint;
    }

    public double getThirdAngle() {
        return thirdAngle;
    }
}
