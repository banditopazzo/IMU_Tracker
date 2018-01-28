package banditopazzo.imu_tracker.tracking.trackingBoard;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import banditopazzo.imu_tracker.R;
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

    private Bitmap redArrow;
    private Bitmap axisIndex;

    //Constructor
    public TrackingSurface(Context context) {
        super(context);

        //Set up holder
        this.holder = getHolder();

        //Set up redArrow
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.redarrow);
        double bitmapScaleFactor = 0.02;
        redArrow = Bitmap.createScaledBitmap(
                original,
                (int) (original.getWidth()  * bitmapScaleFactor),
                (int) (original.getHeight() * bitmapScaleFactor),
                true);
        Log.d(TAG, "Surface created");
    }

    public void setAxisIndex(Bitmap index) {
        this.axisIndex = index;
    }

    //Set up center
    private void setCenter(){
        Canvas c = holder.lockCanvas();

        //Set initial position
        int x = c.getWidth()/2;
        int y = c.getHeight()/2;
        center = new PointD(x,y);

        holder.unlockCanvasAndPost(c);
    }

    //Dato un nuovo punto costruisce una retta dal punto precendente al nuovo punto se questi sono diversi
    @Override
    public void updateSurface(PointD newP, double angle) {
        if (holder.getSurface().isValid()) {

            //Calculate the center the first time
            if (center == null)
                setCenter();

            //Set initial position to the center se è sconosciuta
            if (lastPosition == null)
                lastPosition = new PointD(center.getX(),center.getY());

            //Fattore di scala
            final float SCALA = 1000.0f;

            //Step della griglia
            double gridStep = 0.1; //ogni 10 cm
            //Scala griglia
            gridStep = gridStep * SCALA;

            //Scala i valori del punto
            double newX = newP.getX()*SCALA;
            double newY = newP.getY()*SCALA;

            //Sum the center position to obtain real coordinates
            newX = newX + center.getX();
            newY = newY + center.getY();

            //Set up paint
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(8);

            //Get Canvas
            Canvas c = holder.lockCanvas();

            //Draw Background
            c.drawColor(Color.BLACK);

            //Draw Grid
            drawGrid(c, gridStep);

            //Draw Center - Yellow Point
            p.setColor(Color.YELLOW);
            c.drawPoint((float) center.getX(), (float) center.getY(), p);

            //Draw Tracker - Red Arrow
            //Rotazione
            Matrix matrix = new Matrix();
            matrix.postRotate( (float) Math.toDegrees(angle) );
            Bitmap rotatedRedArrow = Bitmap.createBitmap(
                    redArrow,
                    0,
                    0,
                    redArrow.getWidth(),
                    redArrow.getHeight(),
                    matrix,
                    true
            );
            //Disegno
            c.drawBitmap(
                    rotatedRedArrow,
                    (float) newX - (redArrow.getWidth()/2),
                    (float) newY - (redArrow.getHeight()/2),
                    null
            );

            //Draw Axis Index - Bottom Left
            c.drawBitmap(
                    axisIndex,
                    (float) 0 - (axisIndex.getWidth()/5),
                    (float) c.getHeight() - axisIndex.getHeight(),
                    null
            );

            //Commit changes to the Canvas
            holder.unlockCanvasAndPost(c);
/*
            //Log Old and New positions
            Log.d(TAG, "Last X "+ lastPosition.getX());
            Log.d(TAG, "Last Y "+ lastPosition.getY());
            Log.d(TAG, "New X "+ newX);
            Log.d(TAG, "New Y "+ newY);
            Log.d(TAG, "Angle "+ angle);
*/
            //update last position
            lastPosition.setX(newX);
            lastPosition.setY(newY);
        }
    }

    public void resetPosition() {

        //Delete the stored position
        lastPosition = null;

        Canvas c = holder.lockCanvas();

        //Delete the position from screen
        c.drawColor(Color.BLACK);

        holder.unlockCanvasAndPost(c);

    }

    private void drawGrid(Canvas c, double gridStep) {
        //Grid
        Paint gridColor = new Paint();
        gridColor.setColor(Color.BLUE);
        //from center to right
        double currentGridLinePos = center.getX();
        while (currentGridLinePos < c.getWidth()) {
            c.drawLine(
                    (float) currentGridLinePos,
                    0,
                    (float) currentGridLinePos,
                    c.getHeight(),
                    gridColor
            );
            currentGridLinePos += gridStep;
        }
        //from center to left
        currentGridLinePos = center.getX();
        while (currentGridLinePos >0) {
            c.drawLine(
                    (float) currentGridLinePos,
                    0,
                    (float) currentGridLinePos,
                    c.getHeight(),
                    gridColor
            );
            currentGridLinePos -= gridStep;
        }
        //from center to bottom
        currentGridLinePos = center.getY();
        while (currentGridLinePos < c.getHeight()) {
            c.drawLine(
                    0,
                    (float) currentGridLinePos,
                    c.getWidth(),
                    (float) currentGridLinePos,
                    gridColor
            );
            currentGridLinePos += gridStep;
        }
        //from center to top
        currentGridLinePos = center.getY();
        while (currentGridLinePos > 0) {
            c.drawLine(
                    0,
                    (float) currentGridLinePos,
                    c.getWidth(),
                    (float) currentGridLinePos,
                    gridColor
            );
            currentGridLinePos -= gridStep;
        }
    }

}
