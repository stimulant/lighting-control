package processing.test.barometer;import java.lang.reflect.*;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.ArrayList;
import android.util.Log;
/**
 * Android Accelerometer Sensor Manager Archetype
 * @author antoine vianey
 * under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 */
public class BarometerManager {

  private static float referencePressure; // Cache store of the reference

  private Sensor sensor;
  private SensorManager sensorManager;
  
  private PressuredataObject pdoPrevious = null;
	 private static final String LOG_TAG = "DataCollectorthread";
  Method barometerEventMethod;

  /** indicates whether or not Accelerometer Sensor is supported */
  private Boolean supported;
  /** indicates whether or not Accelerometer Sensor is running */
  private boolean running = false;
  private ArrayList<PressuredataObject> pressureDataList = null;
  private int numReads;
  Context context;


  public BarometerManager(Context parent) {
    this.context = parent;
    numReads = 0;    
    pressureDataList = new ArrayList<PressuredataObject>();
             
    this.referencePressure = PressureUtilities.DEFAULT_REFERENCE_PRESSURE;
    
    try {
      barometerEventMethod =
        parent.getClass().getMethod("barometerEvent", new Class[] { Float.TYPE, Float.TYPE });
    } catch (Exception e) {
      // no such method, or an error.. which is fine, just ignore
    }
//    System.out.println("shakeEventMethod is " + shakeEventMethod);
//    System.out.println("accelerationEventMethod is " + accelerationEventMethod);
    resume();
    
  }
  public void setReferencePressure() {
    if (pdoPrevious != null) {
        referencePressure = pdoPrevious.getAirPressure();
        PressureUtilities.insertReferencePointToDB(
                context, referencePressure);

    }
  }


  public void resume() {
    if (isSupported()) {
      startListening();
    }
  }
  
  
  public void pause() {
    if (isListening()) {
      stopListening();
    }
  }


  /**
   * Returns true if the manager is listening to orientation changes
   */
  public boolean isListening() {
    return running;
  }


  /**
   * Unregisters listeners
   */
  public void stopListening() {
    running = false;
    try {
      if (sensorManager != null && sensorEventListener != null) {
        sensorManager.unregisterListener(sensorEventListener);
      }
    } 
    catch (Exception e) {
    }
  }


  /**
   * Returns true if at least one Accelerometer sensor is available
   */
  public boolean isSupported() {
    if (supported == null) {
      sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
      List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
      supported = new Boolean(sensors.size() > 0);
    }
    return supported;
  }


//  /**
//   * Configure the listener for shaking
//   * @param threshold
//   * 			minimum acceleration variation for considering shaking
//   * @param interval
//   * 			minimum interval between to shake events
//   */
//  public static void configure(int threshold, int interval) {
//    BarometerManager.threshold = threshold;
//    BarometerManager.interval = interval;
//  }


  /**
   * Registers a listener and start listening
   * @param accelerometerListener callback for accelerometer events
   */
  public void startListening() {
//    AccelerometerListener accelerometerListener = (AccelerometerListener) context;
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_PRESSURE);
    if (sensors.size() > 0) {
      sensor = sensors.get(0);
      running = sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
//      listener = accelerometerListener;
    }
  }


//  /**
//   * Configures threshold and interval
//   * And registers a listener and start listening
//   * @param accelerometerListener
//   * 			callback for accelerometer events
//   * @param threshold
//   * 			minimum acceleration variation for considering shaking
//   * @param interval
//   * 			minimum interval between to shake events
//   */
//  public void startListening(int threshold, int interval) {
//    configure(threshold, interval);
//    startListening();
//  }


  /**
   * The listener that listen to events from the accelerometer listener
   */
  //private static SensorEventListener sensorEventListener = new SensorEventListener() {
  private SensorEventListener sensorEventListener = new SensorEventListener() {
    private long now = 0;
    private long timeDiff = 0;
    private long lastUpdate = 0;
    private long lastShake = 0;

    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;
    private float force = 0;

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
            if (sensor.equals(sensor)) {
                Log.d(LOG_TAG, "Accuracy changed on Pressure Sensor");
            } else {
                Log.d(LOG_TAG,
                        "Accuracy changed on Other Sensor, odd beahaviour");
            }

        }

    public void onSensorChanged(SensorEvent sensEvent) {
      // use the event timestamp as reference
      // so the manager precision won't depends 
      // on the AccelerometerListener implementation
      // processing time
      now = sensEvent.timestamp;
      
      		// TODO Auto-generated method stub
      float[] values = sensEvent.values;
      PressureUtilities.insertPressureObjectToList(
      new PressuredataObject(values[0], 0f, System
      .currentTimeMillis()), pressureDataList);
      numReads++;
       
      float speed = 0f;
      float alt = 0f;
    
    
      // Wait until list is full.
      if (numReads >= PressureUtilities.MAXLENGTHOFLIST) {
         // Select median pressure value in order to compensate for eventual
        // peaks.
        PressuredataObject pdo = PressureUtilities
        .selectMedianValue(pressureDataList);
         
        if (pdoPrevious == null) {
          pdo.setSpeed(0);
        } else {
          speed = PressureUtilities.calculateSpeed(pdo, pdoPrevious);
           // Log.d(LOG_TAG, "speed "+speed);
        }
        alt = PressureUtilities.calculateAltitude(pdo, referencePressure);
        pdoPrevious = pdo;
        // trigger change event
        //      listener.onAccelerationChanged(x, y, z);
        if (barometerEventMethod != null) {
          try {
            barometerEventMethod.invoke(context, new Object[] { speed, alt });
          } catch (Exception e) {
            e.printStackTrace();
            barometerEventMethod = null;
          }
        }
      }

    
    
    
    }
  };
}

