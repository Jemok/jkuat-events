package events.domain.com.events;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class EventDetails extends AppCompatActivity {



    /* Variables */
    ParseObject eventObj;
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Check for Write External Storage permission on Android 6
        if (!mmp.checkPermissionForWriteExternalStorage()) {
            mmp.requestPermissionForWriteExternalStorage();
            Log.i("log-", "PERMISSION NOT GRANTED YET");
        } else {
            Log.i("log-", "PERMISSION GRANTED!");
        }


        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        eventObj = ParseObject.createWithoutData(Configs.EVENTS_CLASS_NAME, objectID);
        try { eventObj.fetchIfNeeded().getParseObject(Configs.EVENTS_CLASS_NAME);

            // Set Title on the ActionBar
            getSupportActionBar().setTitle("Event Details");


            // Get Title
            TextView titletxt = (TextView)findViewById(R.id.eTitletxt);
            titletxt.setText(eventObj.getString(Configs.EVENTS_TITLE).toUpperCase());

            // Get Image
             final ImageView eImg = (ImageView)findViewById(R.id.eEventImg);
             ParseFile fileObject = (ParseFile)eventObj.get(Configs.EVENTS_IMAGE);
             if (fileObject != null ) {
                 fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, com.parse.ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            eImg.setImageBitmap(bmp);
             }}}});}


            // Get Description
            TextView descTxt = (TextView)findViewById(R.id.eDescriptionTxt);
            descTxt.setText(eventObj.getString(Configs.EVENTS_DESCRIPTION));








            // MARK: - SHARE EVENT BUTTON ----------------------------------------------------------------
             Button shareButt = (Button)findViewById(R.id.shareEventButt);
             shareButt.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 String messStr = "Check out this event: " + eventObj.getString(Configs.EVENTS_TITLE) + " | from #Events";

                 Bitmap bitmap = ((BitmapDrawable) eImg.getDrawable()).getBitmap();
                 Uri uri = getImageUri(EventDetails.this, bitmap);
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("image/jpeg");
                 intent.putExtra(Intent.EXTRA_STREAM, uri);
                 intent.putExtra(Intent.EXTRA_TEXT, messStr);
                 startActivity(Intent.createChooser(intent, "Share on..."));
             }});



            // Get Start and End dates
             final Date startDate = eventObj.getDate(Configs.EVENTS_START_DATE);
             Date endDate = eventObj.getDate(Configs.EVENTS_END_DATE);


            // Get Month
            final TextView monthTxt = (TextView) findViewById(R.id.eMonthTxt);
            SimpleDateFormat month = new SimpleDateFormat("MMM");
            monthTxt.setText(month.format(startDate).toUpperCase());

            // Get Day
            final TextView dayTxt = (TextView) findViewById(R.id.eDayTxt);
            SimpleDateFormat day = new SimpleDateFormat("dd");
            dayTxt.setText(day.format(startDate));

            // Get Year
            final TextView yearTxt = (TextView) findViewById(R.id.eYearTxt);
            SimpleDateFormat year = new SimpleDateFormat("yyyy");
            yearTxt.setText(year.format(startDate));





            // MARK: - ADD EVENT TO CALENDAR BUTTON -------------------------------------------------
            Button addCalButt = (Button)findViewById(R.id.addToCalButt);
            assert addCalButt != null;
            addCalButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setType("vnd.android.cursor.item/event");
                calIntent.putExtra(CalendarContract.Events.TITLE, eventObj.getString(Configs.EVENTS_TITLE));
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventObj.getString(Configs.EVENTS_LOCATION));
                calIntent.putExtra(CalendarContract.Events.DESCRIPTION, eventObj.getString(Configs.EVENTS_DESCRIPTION));

                SimpleDateFormat m = new SimpleDateFormat("MM");
                String mStr = (m.format(startDate));
                int  yInt = Integer.parseInt(yearTxt.getText().toString());
                int  mInt = Integer.parseInt(mStr);
                int  dInt = Integer.parseInt(dayTxt.getText().toString());

                GregorianCalendar calDate = new GregorianCalendar(yInt, mInt-1, dInt);
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        calDate.getTimeInMillis());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                        calDate.getTimeInMillis());

                startActivity(calIntent);
            }});





            // MARK: - REGISTER NOW BUTTON --------------------------------------------------------
            Button registerButt = (Button)findViewById(R.id.registerButt);

            // Full Date format
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMM dd yyyy @hh:mm a");

            // Check if event is passed
            String endDateStr = fullDateFormat.format(endDate);

            Date strDate = null;
            try {
                strDate = fullDateFormat.parse(endDateStr);
            } catch (java.text.ParseException e) { e.printStackTrace(); }

            if (new Date().after(strDate)) {
                registerButt.setEnabled(false);
                registerButt.setBackgroundColor(Color.parseColor("#888888"));
                registerButt.setText("EVENT PASSED");
                addCalButt.setEnabled(false);
                addCalButt.setBackgroundColor(Color.parseColor("#888888"));
                addCalButt.setText("EVENT PASSED");
            }

            // Open this Event's website to default Browser
            registerButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(eventObj.getString(Configs.EVENTS_WEBSITE))));
            }});





            // Get START date text
            TextView startDateTxt = (TextView) findViewById(R.id.textvi);
            String startStr  = fullDateFormat.format(startDate).toUpperCase();
            startDateTxt.setText("Start Date: " + startStr);

            // Get END date text
            TextView endDateTxt = (TextView) findViewById(R.id.eEndDateTxt);
            String endStr = fullDateFormat.format(endDate).toUpperCase();
            endDateTxt.setText("End Date: " + endStr);

            // Get Cost
            TextView costTxt = (TextView) findViewById(R.id.eCostTxt);
            costTxt.setText("COST: " + eventObj.getString(Configs.EVENTS_COST));


            // Get Website on Button
            Button webButt = (Button)findViewById(R.id.eWebButt);
            if (eventObj.getString(Configs.EVENTS_WEBSITE) != null) {
                webButt.setText("Website: " + eventObj.getString(Configs.EVENTS_WEBSITE));
                webButt.setEnabled(true);
            } else {
                webButt.setText("N/A");
                webButt.setEnabled(false);
            }

            // Open URL to default Browser
            webButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(eventObj.getString(Configs.EVENTS_WEBSITE))));
            }});


            // Get Address
            TextView addTxt = (TextView) findViewById(R.id.eAddressTxt);
            addTxt.setText("Address: " + eventObj.getString(Configs.EVENTS_LOCATION));


            // Open Address in Google Maps Button
            Button openMapButt = (Button)findViewById(R.id.openMapsButt);
            final String addressStr  = "http://maps.google.co.in/maps?q=" + eventObj.getString(Configs.EVENTS_LOCATION);
            openMapButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(addressStr)));
            }});


            // Show Google Maps in WebView
            WebView webView = (WebView)findViewById(R.id.mapWebView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            String locationStr = eventObj.getString(Configs.EVENTS_LOCATION);
            String addStr = locationStr.replace(" ", "+");
            String mapStr = "https://google.com/maps/place/" + addStr;
            webView.loadUrl(mapStr);


        } catch (com.parse.ParseException e) { e.printStackTrace(); }



        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



    } //@end onCreate








    // MENU BUTTONS ------------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // DEFAULT BACK BUTTON
            case android.R.id.home:
                this.finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }





    // Method to get URI of the eventImage
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


} //@end
