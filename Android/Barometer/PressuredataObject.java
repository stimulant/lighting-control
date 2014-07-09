/*
 * Copyright 2011 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * This is a small data container where used to contain data regardning
 * airpressure, time and vertical speed
 * 
 */
public class PressuredataObject {
    private float airPressure;
    private long time;
    private float speed;

    PressuredataObject(float airPressure, float speed, long time) {
        super();
        this.airPressure = airPressure;
        this.time = time;
        this.speed = speed;
    }

    public float getAirPressure() {
        return airPressure;
    }

    public long getTime() {
        return time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

}
