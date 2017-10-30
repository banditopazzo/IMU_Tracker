package banditopazzo.imu_tracker.tracking.trackingBoard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import banditopazzo.imu_tracker.tracking.accelerometer.UpgradableSurface;
import banditopazzo.imu_tracker.tracking.models.PointD;

public class TrackingSurface extends SurfaceView implements UpgradableSurface {

    //Holder
    private SurfaceHolder holder;

    //Logging
    private String TAG = "TrackingSurface";

    //Last position
    private PointD lastPosition;
    private PointD center;

    //Constructor
    public TrackingSurface(Context context) {
        super(context);

        //Set up holder
        this.holder = getHolder();

        Log.d(TAG, "Surface created");
    }

    private void setStartingPoint(){
        Canvas c = holder.lockCanvas();

        //Set initial position
        int x = c.getWidth()/2;
        int y = c.getHeight()/2;
        center = new PointD(x,y);
        lastPosition = new PointD(x,y);

        holder.unlockCanvasAndPost(c);
    }

    //Dato un nuovo punto costruisce una retta dal punto precendente al nuovo punto se questi sono diversi
    @Override
    public void updateSurface(PointD newP) {
        if (holder.getSurface().isValid()) {

            //Calculate the center if the first time
            if (lastPosition == null)
                setStartingPoint();

            //Scala i valori
            final float SCALA = 3000.0f;
            double newX = newP.getX()*SCALA;
            double newY = newP.getY()*SCALA;

            //Sum the center position to obtain real coordinates
            newX = newX + center.getX();
            newY = newY + center.getY();

            //If the position doesn't change, don't do anything
            if (newX == lastPosition.getX() && newY == lastPosition.getY())
                return;

            //Get Canvas
            Canvas c = holder.lockCanvas();

            //Set up paint
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(8);
            p.setColor(Color.RED);

            //Draw line
            c.drawLine(
                    (float) lastPosition.getX(),
                    (float) lastPosition.getY(),
                    (float) newX,
                    (float) newY,
                    p
            );

            //Commit changes
            holder.unlockCanvasAndPost(c);

            //Log Old and New positions
            Log.d(TAG, "Last X "+ lastPosition.getX());
            Log.d(TAG, "Last Y "+ lastPosition.getY());
            Log.d(TAG, "New X "+ newX);
            Log.d(TAG, "New Y "+ newY);

            //update last position
            lastPosition.setX(newX);
            lastPosition.setY(newY);
        }
    }

}
