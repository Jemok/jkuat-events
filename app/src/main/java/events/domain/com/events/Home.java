package events.domain.com.events;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Home extends AppCompatActivity {

    /* Views */
    ProgressDialog progDialog;
    EditText searchTxt;


    /* Variables */
    List<ParseObject> eventsArray = null;

    private static final String TAG = Home.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    private GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title on the ActionBar
        getSupportActionBar().setTitle("Events");


        // Init a ProgressDialog
        progDialog = new ProgressDialog(this);
        progDialog.setTitle(R.string.app_name);
        progDialog.setMessage("Loading...");
        progDialog.setIndeterminate(false);


        // Init TabBar buttons
        Button tab_one = (Button) findViewById(R.id.tab_one);
        Button tab_two = (Button) findViewById(R.id.tab_two);
        Button tab_three = (Button) findViewById(R.id.tab_three);


        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, SubmitEvent.class));
            }
        });

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Mail chooser
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{Configs.CONTACT_EMAIL_ADDRESS});
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Contact/Feedback message from Events");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, "Send"));
            }
        });


        // MARK: - SEARCH TXT ON TOUCH LISTENER -------------------------------------------
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                searchTxt.setText("");
                return false;
            }
        });


        // MARK: - SERACH BUTTON ------------------------------------------------
        Button searchButt = (Button) findViewById(R.id.searchButt);
        searchButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismisskeyboard();
                // Recall query with search text
                queryEvents();
            }
        });


        // Call query
        queryEvents();


        // Init AdMob banner
//        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

    } // @end onCreate()


    // MARK: - QUERY EVENTS -----------------------------------------------------------------------------
    public void queryEvents() {
        progDialog.show();

        ParseQuery query = ParseQuery.getQuery(Configs.EVENTS_CLASS_NAME);
        query.whereEqualTo(Configs.EVENTS_IS_PENDING, false);

        // Get keywords (if any)
        String searchStr = searchTxt.getText().toString();
        List<String> keywords = new ArrayList<String>();
        String[] one = searchStr.toLowerCase().split(" ");
        for (String keyw : one) {
            keywords.add(keyw);
        }
        Log.i("log-", "KEYWORDS" + keywords + "\n");

        if (!searchStr.matches("")) {
            query.whereContainedIn(Configs.EVENTS_KEYWORDS, keywords);
        }

        query.orderByDescending(Configs.EVENTS_END_DATE);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    eventsArray = objects;
                    progDialog.dismiss();


                    // CUSTOM GRID ADAPTER
                    class GridAdapter extends BaseAdapter {
                        private Context context;

                        public GridAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.event_cell, null);
                            }
                            // Get Parse object
                            ParseObject eventObj = eventsArray.get(position);


                            // Get Month
                            TextView monthTxt = (TextView) cell.findViewById(R.id.monthTxt);
                            Date startDate = eventObj.getDate(Configs.EVENTS_START_DATE);
                            Date endDate = eventObj.getDate(Configs.EVENTS_END_DATE);
                            SimpleDateFormat month = new SimpleDateFormat("MMM");
                            monthTxt.setText(month.format(startDate));

                            // Get Day number
                            TextView dayTxt = (TextView) cell.findViewById(R.id.dayTxt);
                            SimpleDateFormat day = new SimpleDateFormat("dd");
                            dayTxt.setText(day.format(startDate));

                            // Get Year
                            TextView yearTxt = (TextView) cell.findViewById(R.id.yearTxt);
                            SimpleDateFormat year = new SimpleDateFormat("yyyy");
                            yearTxt.setText(year.format(startDate));

                            // Get Title
                            TextView titleTxt = (TextView) cell.findViewById(R.id.titleTxt);
                            titleTxt.setText(eventObj.getString(Configs.EVENTS_TITLE).toUpperCase());

                            // Get Address
                            TextView addTxt = (TextView) cell.findViewById(R.id.addressTxt);
                            addTxt.setText(eventObj.getString(Configs.EVENTS_LOCATION));

                            // Get Start & End dates
                            TextView dateTxt = (TextView) cell.findViewById(R.id.dateTxt);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy | hh:mm a");
                            dateTxt.setText(dateFormat.format(startDate).toUpperCase() + " - " + dateFormat.format(endDate).toUpperCase());

                            // Get Cost
                            TextView costTxt = (TextView) cell.findViewById(R.id.costTxt);
                            costTxt.setText(eventObj.getString(Configs.EVENTS_COST));

                            // Get Image
                            final ImageView eventImage = (ImageView) cell.findViewById(R.id.eventImage);
                            ParseFile fileObject = (ParseFile) eventObj.get(Configs.EVENTS_IMAGE);
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            eventImage.setImageBitmap(bmp);
                                        }
                                    }
                                }
                            });


                            return cell;
                        }

                        @Override
                        public int getCount() {
                            return eventsArray.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return eventsArray.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }

                    }


                    // Init GridView and set its adapter
                    GridView eventsGrid = (GridView) findViewById(R.id.eventsGridView);
                    eventsGrid.setAdapter(new GridAdapter(Home.this, eventsArray));
                    eventsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            ParseObject eventObj = eventsArray.get(position);

                            Intent i = new Intent(Home.this, EventDetails.class);
                            Bundle extras = new Bundle();
                            extras.putString("objectID", eventObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);

                        }
                    });


                    // Error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), Home.this);
                    progDialog.dismiss();
                }
            }
        });

    }


    // MENU BUTTON ON ACTION BAR -------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // Refresh Button
            case R.id.refreshButt:
                searchTxt.setText("");
                dismisskeyboard();
                queryEvents();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }


    // MARK : - DISMISS KEYBOARD ------------------------------------------------------------------------------
    public void dismisskeyboard() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
    }

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, Home.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                request,
                createGeofencePendingIntent()
        ).setResultCallback((ResultCallback<? super Status>) this);
    }


    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            saveGeofence();
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = map.addCircle( circleOptions );
    }

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";
    private Marker geoFenceMarker;
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if ( map!=null ) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = map.addMarker(markerOptions);

        }
    }

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( geoFenceMarker.getPosition().latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( geoFenceMarker.getPosition().longitude ));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
            markerForGeofence(latLng);
            drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }


} //@end
