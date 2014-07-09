BarometerManager barometer;
float speed,altitude;
float maxAlt;
PImage dot;
OPC opc;
float average=0;
float sum = 0;
float[] storedValues;
int count = 0;

void setup() {
  barometer = new BarometerManager(this);
  orientation(PORTRAIT);
  // Load a sample image
  dot = loadImage("color-dot.png");

  // Connect to the local instance of fcserver
  opc = new OPC(this, "192.168.2.1", 7890);

  // Map one 64-LED strip to the center of the window
  opc.ledStrip(0, 64, width/2, height/2, width / 70.0, 0, false);
  maxAlt = 21.0;
  storedValues = new float[10];
  println("width " + width + " height " + height );

  //noLoop();
}


void draw() {
   colorMode(HSB, 360,100,100);
  background(63,82,100*(altitude/maxAlt));
  //
 
  textSize(70);
  textAlign(CENTER, TOP);
  //colorMode(RGB, 255);
   fill(100);
  text("speed: " + nf(speed, 1, 2) + "\n" + 
       "altitude: " + nf(altitude, 1, 2) + "\n", 
       0, 0, width, height);
   // println("altitude: " + altitude );    
  
  
}


public void resume() {
  if (barometer != null) {
    barometer.resume();
  }
}

void mousePressed()
{
   barometer.setReferencePressure();
}
    
public void pause() {
  if (barometer != null) {
    barometer.pause();
  }
}

public void barometerEvent(float s, float a) {
//  println("acceleration: " + x + ", " + y + ", " + z);
  speed = s;
  
  AddNewValue(a);
  float ave = 0;
  
  if(count > 0)
  {
    ave = sum / count;
  }
  altitude = ave;
  redraw();
}

void AddNewValue(float val)
{
  if(count < storedValues.length)
  {
    //array is not full yet
    storedValues[count++] = val;
    
    sum += val; 
  }
  else
  {
    sum += val; 
    sum -= storedValues[0];
    
    //shift all of the values, drop the first one (oldest) 
    for(int i = 0; i < storedValues.length-1; i++)
    {
      storedValues[i] = storedValues[i+1] ;
    }
    //the add the new one
    storedValues[storedValues.length-1] = val;
  }
}
