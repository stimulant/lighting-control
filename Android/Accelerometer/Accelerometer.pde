AccelerometerManager accel;
float ax, ay, az;
OPC opc;

void setup() {
  accel = new AccelerometerManager(this);
  orientation(PORTRAIT);
  noLoop();
   size(800, 200);
   
  // Connect to the local instance of fcserver
  opc = new OPC(this, "192.168.2.30", 7890);

  // Map one 64-LED strip to the center of the window
  opc.ledStrip(0, 64, width/2, height/2, width / 70.0, 0, false);
}


void draw() {
  background(0);
  fill(255);
  textSize(70);
  textAlign(CENTER, CENTER);
  //text("x: " + nf(ax, 1, 2) + "\n" + 
  //     "y: " + nf(ay, 1, 2) + "\n" + 
  //     "z: " + nf(az, 1, 2), 
  //     0, 0, width, height);
  colorMode(HSB, 9);  // Use HSB with scale of 0-100
color c = color(ay+9/18, 9, 9);  // Update 'c' with new color
fill(c);
  rect(0,0,width,height);
}


public void resume() {
  if (accel != null) {
    accel.resume();
  }
}

    
public void pause() {
  if (accel != null) {
    accel.pause();
  }
}


public void shakeEvent(float force) {
  println("shake : " + force);
}


public void accelerationEvent(float x, float y, float z) {
//  println("acceleration: " + x + ", " + y + ", " + z);
  ax = x;
  ay = y;
  az = z;
  redraw();
}
