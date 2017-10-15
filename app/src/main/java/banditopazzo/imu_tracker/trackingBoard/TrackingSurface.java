package banditopazzo.imu_tracker.trackingBoard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import banditopazzo.imu_tracker.accelerometer.AccListener;
import banditopazzo.imu_tracker.models.PointD;
import banditopazzo.imu_tracker.gyroscope.GyroListener;

public class TrackingSurface extends SurfaceView implements UpgradableSurface{

    //Holder
    private SurfaceHolder holder;

    //Logging
    private String TAG = "TrackingSurface";

    //Last position
    private PointD lastPosition;
    private PointD center;

    //Sensors
    private SensorManager SM;
    private Sensor accelerometer, gyroscope;

    //Listeners
    private AccListener accListener;
    private GyroListener gyroListener;

    //Handler
    private HandlerThread handlerThread;
    private Handler handler;

    //Constructor
    public TrackingSurface(Context context) {
        super(context);
        this.holder = getHolder();

        //Create sensor manager
        SM = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //Get Accelerometer
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Get Gyroscope
        gyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //Set up Handler
        this.handlerThread = new HandlerThread("SensorHandlerThread");
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());

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

    public void start(){

        //Attiva Listeners
        gyroListener = new GyroListener();
        SM.registerListener(gyroListener, gyroscope, SensorManager.SENSOR_DELAY_GAME, handler);
        accListener = new AccListener(this, gyroListener);
        SM.registerListener(accListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,handler);

    }


    public void stop(){
        //Stop Listeners
        SM.unregisterListener(gyroListener);
        SM.unregisterListener(accListener);
        //Delete Listeners
        gyroListener=null;
        accListener=null;
        handler = null;
        handlerThread.quit(); //TODO: meglio quitSafely(), modificare l'API target
        //TODO: cancella il percorso
    }

    public void pause() {
        //TODO: controllare il funzionamento
        SM.unregisterListener(gyroListener);
        SM.unregisterListener(accListener);
    }

    public void resume() {
        //TODO: controllare il funzionamento
        SM.registerListener(gyroListener, gyroscope, SensorManager.SENSOR_DELAY_GAME, handler);
        SM.registerListener(accListener, accelerometer, SensorManager.SENSOR_DELAY_GAME,handler);
    }

    //Dato un nuovo punto costruisce una retta dal punto precendente al nuovo punto se questi sono diversi
    @Override
    public void updateSurface(PointD newP) {
        if (holder.getSurface().isValid()) {

            //Calculate the center if the first time
            if (lastPosition == null)
                setStartingPoint();

            //Sum the center position to obtain real coordinates
            double newX = newP.getX() + center.getX();
            double newY = newP.getY() + center.getY();

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
