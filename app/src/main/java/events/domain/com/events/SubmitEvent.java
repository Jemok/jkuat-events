package events.domain.com.events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SubmitEvent extends AppCompatActivity {


    /* Views */
    ProgressDialog progDialog;
    EditText nametxt, locTxt, desctxt, costtxt, webTxt;
    ImageView evImage;



    /* Variables */
    private Date startDate;
    private Date endDate;
    String stDateStr;
    String endDateStr;
    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_event);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Title on the ActionBar
        getSupportActionBar().setTitle("Submit an Event");


        // Init a ProgressDialog
        progDialog = new ProgressDialog(SubmitEvent.this);
        progDialog.setTitle(R.string.app_name);
        progDialog.setIndeterminate(false);


        // Init views
        nametxt = (EditText)findViewById(R.id.eNameTxt);
        locTxt = (EditText)findViewById(R.id.eLocationTxt);
        desctxt = (EditText)findViewById(R.id.eDescriptionTxt);
        costtxt = (EditText)findViewById(R.id.ecostTxt);
        webTxt = (EditText)findViewById(R.id.eWebTxt);
        evImage = (ImageView)findViewById(R.id.evImage);



        // Init TabBar buttons
        Button tab_one = (Button)findViewById(R.id.tab_one);
        Button tab_two = (Button)findViewById(R.id.tab_two);
        Button tab_three = (Button)findViewById(R.id.tab_three);


        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SubmitEvent.this, Home.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Mail chooser
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{Configs.CONTACT_EMAIL_ADDRESS});
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Contact/Feedback message from Events");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent,"Send"));
            }});






        // SET START DATE BUTTONS ------------------------------------------------------------------------
        final Button startDateButt = (Button) findViewById(R.id.eStartDateButt);
        startDateButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();

                // Get Current Date
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SubmitEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startDateButt.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });


        final Button startTimeButt = (Button) findViewById(R.id.eStartTimeButt);
        startTimeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();

                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(SubmitEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                startTimeButt.setText(hourOfDay + ":" + minute);

                                // Set START DATE
                                stDateStr = startDateButt.getText().toString() + startTimeButt.getText().toString();
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyyhh:mm");
                                try {
                                    startDate = format.parse(stDateStr);
                                    // Toast.makeText(SubmitEvent.this, startDate.toString(), Toast.LENGTH_SHORT).show();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }



                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });





        // SET END DATE BUTTONS --------------------------------------------------------------------------------
        final Button endDateButt = (Button) findViewById(R.id.eEndDateButt);
        endDateButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();

                // Get Current Date
                final Calendar c = Calendar.getInstance();
                int year2 = c.get(Calendar.YEAR);
                int month2 = c.get(Calendar.MONTH);
                int day2 = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(SubmitEvent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                endDateButt.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year2, month2, day2);
                datePickerDialog.show();
            }
        });

        final Button endTimeButt = (Button) findViewById(R.id.eEndTimeButt);
        endTimeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();

                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int hour2 = c.get(Calendar.HOUR_OF_DAY);
                int minute2 = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(SubmitEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                endTimeButt.setText(hourOfDay + ":" + minute);

                                // Set START DATE
                                endDateStr = endDateButt.getText().toString() + endTimeButt.getText().toString();
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyyhh:mm");
                                try {
                                    endDate = format.parse(endDateStr);
                                    // Toast.makeText(SubmitEvent.this, startDate.toString(), Toast.LENGTH_SHORT).show();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, hour2, minute2, false);
                timePickerDialog.show();
            }
        });




        // ADD IMAGE --------------------------------------------------------------------------------
        final ImageView evImage = (ImageView)findViewById(R.id.evImage);
        evImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SubmitEvent.this);
                builder.setTitle("SELECT SOURCE");
                builder.setItems(new CharSequence[]
                                {"Take a picture", "Pick from Gallery" },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    // Open Camera
                                    case 0:
                                        if (!marshMallowPermission.checkPermissionForCamera()) {
                                            marshMallowPermission.requestPermissionForCamera();
                                        } else {
                                            openCamera();
                                        }

                                        break;

                                    // Open Gallery
                                    case 1:
                                        if (!marshMallowPermission.checkPermissionForReadExternalStorage()) {
                                            marshMallowPermission.requestPermissionForReadExternalStorage();
                                        } else {
                                            openGallery();
                                        }

                                        break;
                                }
                            }
                        });
                builder.create().show();
            }
        });








        // SUBMIT EVENT BUTTON ----------------------------------------------------------------------
        final Button submitButt = (Button)findViewById(R.id.submitButt);
        submitButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();
                progDialog.setMessage("Submitting event...");
                progDialog.show();


                // FILL ALL THE FIELDS TO SUBMIT AN EVENT...
                if (nametxt.getText().toString().matches("") ||
                        locTxt.getText().toString().matches("")  ||
                        costtxt.getText().toString().matches("")  ||
                    desctxt.getText().toString().matches("")     ||
                    webTxt.getText().toString().matches("")      ) {

                    progDialog.dismiss();

                    Configs.simpleAlert("You must fill all the fields and add an image to submit your Event!", SubmitEvent.this);



                // YOU CAN SUBMIT YOUR EVENT!
                } else {

                    // Create ParseObject
                    ParseObject eventObj = new ParseObject(Configs.EVENTS_CLASS_NAME);

                    // Save data
                    eventObj.put(Configs.EVENTS_IS_PENDING, true);
                    eventObj.put(Configs.EVENTS_TITLE, nametxt.getText().toString());
                    eventObj.put(Configs.EVENTS_LOCATION, locTxt.getText().toString());
                    eventObj.put(Configs.EVENTS_COST, costtxt.getText().toString());
                    eventObj.put(Configs.EVENTS_DESCRIPTION, desctxt.getText().toString());
                    eventObj.put(Configs.EVENTS_WEBSITE, webTxt.getText().toString());

                    // Make keywords
                    List<String> keywords = new ArrayList<String>();
                    String[] one = desctxt.getText().toString().toLowerCase().split(" ");
                    String[] two = nametxt.getText().toString().toLowerCase().split(" ");
                    String[] three = locTxt.getText().toString().toLowerCase().split(" ");
                    for (String keyw : one) { keywords.add(keyw); }
                    for (String keyw : two) { keywords.add(keyw); }
                    for (String keyw : three) { keywords.add(keyw); }
                    Log.i("log-", "KEYWORDS" + keywords + "\n");

                    eventObj.put(Configs.EVENTS_KEYWORDS, keywords);


                    // Save Start & End dates
                    eventObj.put(Configs.EVENTS_START_DATE, startDate);
                    eventObj.put(Configs.EVENTS_END_DATE, endDate);


                    // Save image
                    Bitmap bitmap = ((BitmapDrawable) evImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                    eventObj.put(Configs.EVENTS_IMAGE, imageFile);

                    // Saving block
                    eventObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException error) {
                            if (error == null) {
                                progDialog.dismiss();

                                Configs.simpleAlert("You've successfully submitted your event!\nWe'll review it asap and publish it if it'll be ok", SubmitEvent.this);

                            // error
                            } else {
                                Configs.simpleAlert(error.getMessage(), SubmitEvent.this);
                                progDialog.dismiss();
                    }}
                    });


                }//end IF


            }});


    } //@end onCreate()








// IMAGE HANDLING METHODS ------------------------------------------------------------------------
int CAMERA = 0;
int GALLERY = 1;
Uri imageURI;


// OPEN CAMERA
public void openCamera() {
    Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    File file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
    imageURI = Uri.fromFile(file);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
    startActivityForResult(intent, CAMERA);
}

// OPEN GALLERY
public void openGallery() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
}



// IMAGE PICKED DELEGATE -----------------------------------
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == Activity.RESULT_OK) {
        Bitmap bm = null;

        // Image from Camera
        if (requestCode == CAMERA) {
            try {
                bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                Cursor cur = managedQuery(imageURI, orientationColumn, null, null, null);
                int orientation = -1;
                if (cur != null && cur.moveToFirst()) {
                    orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
            }  catch (IOException e) { e.printStackTrace(); }

        // Image from Gallery
        } else if (requestCode == GALLERY) {
            try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) { e.printStackTrace(); }
        }

        // Set image
        ImageView img = (ImageView)findViewById(R.id.evImage);
        img.setImageBitmap(bm);
    }
}
//---------------------------------------------------------------------------------------------








    // DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nametxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(locTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(desctxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(costtxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(webTxt.getWindowToken(), 0);
    }






    // BACK BUTTON ------------------------------------------------------------------------------
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



} //@end
