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

    //Last position and center variables
    private PointD lastPosition;
    private PointD center;

    //Bitmaps
    private Bitmap staticBitmap;
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

    @Override
    public void updateSurface(PointD newP, double angle) {
        if (holder.getSurface().isValid()) {

            //Calculate the center the first time
            if (center == null)
                setCenter();

            //Set initial position to the center if it's unknown
            if (lastPosition == null)
                lastPosition = new PointD(center.getX(),center.getY());

            //Scale factor
            final float SCALA = 1000.0f;

            //Set up gridStep and scale it
            double gridStep = 0.1; //every 10 cm
            gridStep = gridStep * SCALA;

            //Scala i valori del punto
            double newX = newP.getX()*SCALA;
            double newY = newP.getY()*SCALA;

            //Sum the center position to obtain real coordinates
            newX = newX + center.getX();
            newY = newY + center.getY();

            //Get Canvas
            Canvas c = holder.lockCanvas();

            //Draw background and grid
            if (staticBitmap == null) {
                staticBitmap = createStaticBitmap(c, Color.BLACK, gridStep);
            }
            c.drawBitmap(staticBitmap, 0, 0, new Paint());

            //Draw Tracker - Red Arrow
            drawTracker(c, newX, newY, angle);

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

    private Bitmap createStaticBitmap(Canvas c, int color, double gridStep) {
        Bitmap bitmap = Bitmap.createBitmap(c.getWidth(), c.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(bitmap);

        //Draw Background
        localCanvas.drawColor(color);

        //Draw Grid
        drawGrid(localCanvas, gridStep);

        //Draw Center - Yellow Point
        //Set up paint
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(8);
        p.setColor(Color.YELLOW);
        //Draw
        localCanvas.drawPoint((float) center.getX(), (float) center.getY(), p);

        return bitmap;

    }

    private void drawTracker(Canvas c, double newX, double newY, double angle) {
        //Set up paint
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(8);
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
    }

}
