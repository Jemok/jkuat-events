package events.domain.com.events;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
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

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";


    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, Home.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

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
        Button tab_four = (Button) findViewById(R.id.tab_four);

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

        tab_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Map.class));
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

                            if(fileObject != null){
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
                            }

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

} //@end
