package events.domain.com.events;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;


import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;


public class Configs extends  Application {



    // REPLACE THE EMAIL ADDRESS BELOW WITH THE ONE YOU'LL DEDICATE TO USERS TO DIRECTLY CONTACT YOU
    public static String CONTACT_EMAIL_ADDRESS = "contact@mydomain.com";



    // PARSE KEYS ----------------------------------------------------------------------------
    public static String PARSE_APP_KEY = "TZpy81Eds7ngtGAKOwQTcUSpmAhKpGODRQHmQrWb";
    public static String PARSE_CLIENT_KEY = "TnHTVAMv4dFZKqO9Lck9P0IesaOABF785SEmwDK3";






    /************** DO NOT EDIT THE CODE BELOW! **************/
    public static String EVENTS_CLASS_NAME = "Events";
    public static String EVENTS_TITLE = "title";
    public static String EVENTS_DESCRIPTION = "description";
    public static String EVENTS_WEBSITE = "website";
    public static String EVENTS_LOCATION = "location";
    public static String EVENTS_START_DATE = "startDate";
    public static String EVENTS_END_DATE = "endDate";
    public static String EVENTS_COST = "cost";
    public static String EVENTS_IMAGE = "image";
    public static String EVENTS_IS_PENDING = "isPending";
    public static String EVENTS_KEYWORDS = "keywords";


    boolean isParseInitialized = false;

    public void onCreate() {
        super.onCreate();

        // Init Parse on back4app
        if (isParseInitialized == false) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_KEY))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;


            // Register for Push Notifications
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();

            // REPLACE "450925296457" WITH YOUR OWN GCM Sender ID
            installation.put("GCMSenderId", "450925296457");

            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("log-", "REGISTERED FOR PUSH NOTIFICATIONS");
            }});

        }


    }// end onCreate()




    // MARK: - SIMPLE ALERT DIALOG -----------------------
    public static void simpleAlert(String mess, Context ctx) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setMessage(mess)
            .setTitle(R.string.app_name)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.logo);
        alert.create().show();
    }


} //@end
