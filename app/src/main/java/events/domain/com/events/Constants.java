package events.domain.com.events;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by root on 2/6/18.
 */

public class Constants {
    public static final String GEOFENCE_ID_STAN_UNI = "JKUAT_UNI";
    public static final float GEOFENCE_RADIUS_IN_METERS = 90;

    /**
     * Map for storing information about stanford university in the Stanford.
     */
    public static final HashMap<String, LatLng> AREA_LANDMARKS = new HashMap<String, LatLng>();

    static {
        // JKUAT University.
        AREA_LANDMARKS.put(GEOFENCE_ID_STAN_UNI, new LatLng(37.01050800000007,  -1.0891064));

        // JKUAT Library
        AREA_LANDMARKS.put("Library", new LatLng(37.01535409999997,-1.09694));

        // Engineering Place
        AREA_LANDMARKS.put("Engineering complex", new LatLng(37.01204419999999, -1.0952202));

        // Engineering Place
        AREA_LANDMARKS.put("Agriculture offices", new LatLng(37.0138025, -1.0945159));
    }
}
