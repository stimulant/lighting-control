package processing.test.barometer;/*
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
 * This class contains a set of utility function used to process the collected data 
 * from the air pressure sensor.
 * 
 */
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


public class PressureUtilities {
    public static final int MAXLENGTHOFLIST = 3; // Number of samples to take
    // before calcualting the
    // median value
    static float DEFAULT_REFERENCE_PRESSURE = 1016f; // Default air pressure at
    // sea level.
    static final int INTERVAL_MS = 100; // Wait time between each sample from

    // sensor.

    /**
     * An insert sorting algorithm implemented in order to sort the list of
     * inserted Pressure data objects with regards to pressure. ArrayList "pdoa"
     * is sorted.
     */
    public static void insertSort(PressuredataObject[] pdoa) {
        for (int i = 0; i < pdoa.length; i++) {
            int j = i;
            PressuredataObject pd = pdoa[i];
            while ((j > 0)
                    && (pdoa[j - 1].getAirPressure() > pd.getAirPressure())) {
                pdoa[j] = pdoa[j - 1];
                j--;
            }
            pdoa[j] = pd;
        }
    }

    /**
     * 
     * @param pressureList
     *            Insert set of pressuredataobjects
     * @return The Object with the median air pressure value.
     */
    public static PressuredataObject selectMedianValue(
            ArrayList<PressuredataObject> pressureList) {
        /**
         * This is really a sorting algorithm where we take the mid value.
         */
        PressuredataObject[] pdoa = new PressuredataObject[pressureList.size()];
        pdoa[0] = pressureList.get(0);
        for (int i = 0; i < pressureList.size(); i++) {
            pdoa[i] = pressureList.get(i);
        }
        insertSort(pdoa);
        int index = (MAXLENGTHOFLIST / 2) - 1;
        if (MAXLENGTHOFLIST % 2 == 1) { // Add one to get median value
            index++;
        }
        return pdoa[index];
    }

    /**
     * This function inserts the selected reference pressure to the content
     * provider.
     * 
     * @param ctxt
     *            Context of the application
     * @param refPressure
     *            The Air pressure at the reference point
     * @return Number of inserted values, 1 if successful, -1 if unsuccessful.
     */
    public static int insertReferencePointToDB(Context ctxt, float refPressure) {
        ContentValues cv = new ContentValues();
        cv.put(PressureDataProvider.REFERENCEPOINT_COLUMN_NAME, refPressure);
        Uri uri = ctxt.getContentResolver().insert(
                PressureDataProvider.REFERENCE_CONTENT_URI, cv);
        if (uri != null) {
            Integer id = Integer.decode(uri.getLastPathSegment());
            return id.intValue();
        } else {
            return -1;
        }
    }

    /**
     * This functione returns the air pressure at the reference point.
     * 
     * @param ctxt
     *            Context of the applicatio
     * @return The value of the stored reference value, -1 if no value has been
     *         stored.
     */
    public static float getReferencePoint(Context ctxt) {
        Uri uri = PressureDataProvider.REFERENCE_CONTENT_URI;
        float refPoint = -1;
        Cursor cur = ctxt.getContentResolver().query(uri, null, null, null,
                null);
        if (cur != null) {

            int index = cur
                    .getColumnIndex(PressureDataProvider.REFERENCEPOINT_COLUMN_NAME);
            if (index != -1) {
                if ((cur.getCount() > 0) && (cur.moveToFirst())) {
                    refPoint = cur.getFloat(index);
                    return refPoint;
                }
            }
        }
        return refPoint;
    }

    /**
     * Inserts a pressure data object to an array list of fixed length
     * 
     * @param pdo
     *            Pressure data object.
     * @param list
     *            ArrayList of objects
     */
    public static void insertPressureObjectToList(PressuredataObject pdo,
            ArrayList<PressuredataObject> list) {
        list.add(pdo);
        if (list.size() > MAXLENGTHOFLIST) {
            list.remove(0);
        }
    }

    /**
     * 
     * @param pdo
     *            Current air pressure object
     * @param refPressure
     * @return Calculated altitude in meters above reference point.
     */
    public static float calculateAltitude(PressuredataObject pdo,
            float refPressure) {
        if (pdo != null) {
            return (refPressure - pdo.getAirPressure()) * 8; // Approximately 8m
            // altitude
            // difference
            // per mbar
        } else {
            return 0f;
        }
    }

    /**
     * 
     * @param pdoPres
     *            Current air pressure object
     * @param pdoPrev
     *            Previous air pressure object
     * @return Calculated speed in m/s.
     */
    public static float calculateSpeed(PressuredataObject pdoPres,
            PressuredataObject pdoPrev) {
        if ((pdoPres != null) && (pdoPrev != null)) {
            float pressureNew = pdoPres.getAirPressure();
            float pressureOld = pdoPrev.getAirPressure();
            // Lower air pressure gives higher speed.
            float deltaPressure = pressureOld - pressureNew;
            long deltatime = pdoPres.getTime() - pdoPrev.getTime();
            float speed = 0;
            if (deltatime != 0) {
                speed = 1000 * deltaPressure / deltatime; // Unit is m/s
            }

            return speed;
        } else {
            return 0f;
        }
    }
}
