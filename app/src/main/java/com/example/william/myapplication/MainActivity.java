package com.example.william.myapplication;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static ArrayList<Friend> friendList;
    private static TextView friendList_Text;
    protected static final String TAG = "location-updates-sample";
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private Firebase myFirebaseRef = new Firebase("https://dazzling-heat-5469.firebaseio.com/");
    private HashMap<String, double[]> locations;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected GoogleApiClient mGoogleApiClient;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    protected Boolean mRequestingLocationUpdates;

    protected String mLastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        friendList = new ArrayList<>();
        friendList_Text = (TextView) findViewById(R.id.test);
        dataBase();


        // Locate the UI widgets.
//        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
//        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
//        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
//        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        friendList.add(new Friend("William Hsu", "5103649006"));

        ImageView buttonImage = (ImageView) findViewById(R.id.mybutton);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFancyMethod(v);
            }
        });
    }
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
//                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }
        //need current phonenumbers
        locations.put("currentPhonenumbers", new double[]{mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()});
        myFirebaseRef.child("PhoneNumbers").setValue(locations);
//        if (mRequestingLocationUpdates) {
        startLocationUpdates();
//        }
    }

//    public void startUpdatesButtonHandler(View view) {
//        if (!mRequestingLocationUpdates) {
//            mRequestingLocationUpdates = true;
//            startLocationUpdates();
//        }
//    }

//    public void stopUpdatesButtonHandler(View view) {
//        if (mRequestingLocationUpdates) {
//            mRequestingLocationUpdates = false;
//            setButtonsEnabledState();
//            stopLocationUpdates();
//        }
//    }


//    private void setButtonsEnabledState() {
//        if (mRequestingLocationUpdates) {
//            mStartUpdatesButton.setEnabled(false);
//        } else {
//            mStartUpdatesButton.setEnabled(true);
//            mStopUpdatesButton.setEnabled(false);
//        }
//    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLastUpdateTime);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    public void dataBase() {
        final String phoneNumber = "5103649006";
        final ArrayList<Long> gps = new ArrayList<>();
//        try {
//            //Modes: MODE_PRIVATE, MODE_WORLD_READABLE, MODE_WORLD_WRITABLE
//            FileOutputStream output = openFileOutput("lines.txt",MODE_WORLD_READABLE);
//            DataOutputStream dout = new DataOutputStream(output);
//            dout.writeInt(friendList.size()); // Save line count
//            for(Friend line : friendList) // Save lines
//                dout.writeUTF(line.toString());
//            dout.flush(); // Flush stream ...
//            dout.close(); // ... and close.
//        }
//        catch (IOException exc) { exc.printStackTrace(); }
//        SharedPreferences appSharedPrefs = PreferenceManager
//                .getDefaultSharedPreferences(this.getApplicationContext());
//        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(friendList);
//        prefsEditor.putString("MyObject", json);
//        prefsEditor.commit();
        SharedPreferences prefs = getSharedPreferences("Friend", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("FriendList", ObjectSerializer.serialize(friendList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
        if (mLastLocation != null) {
            gps.add((long) mLastLocation.getLongitude());
            gps.add((long) mLastLocation.getLatitude());
        } else {
            gps.add((long) 0);
            gps.add((long) 0);
        }
        myFirebaseRef.child("PhoneNumbers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                locations = (HashMap) snapshot.getValue();
                ArrayList<String> friends = new ArrayList<>();
//                SharedPreferences appSharedPrefs = PreferenceManager
//                        .getDefaultSharedPreferences(this.getApplicationContext());
//                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
//                Gson gson = new Gson();
//                String json = appSharedPrefs.getString("MyObject", "");
//                Friend test = gson.fromJson(json, Friend.class);
                ArrayList friendList = new ArrayList();
                SharedPreferences prefs = getSharedPreferences("Friend", Context.MODE_PRIVATE);
                try {
                    friendList = (ArrayList) ObjectSerializer.deserialize(prefs.getString("FriendList", ObjectSerializer.serialize(new ArrayList())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Friend s : friendList) {
                    if ((Math.abs(locations.get(s.getNumber())[0] - gps.get(0)) < 1 ) && Math.abs(locations.get(s.getNumber())[1] - gps.get(1)) < 1 ) {
                        friends.add(s.getName());
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

//    public class GPS {
//        private int[] gpsArray;
//
//        public GPS {
//
//        }
//        public int[] getGps() {
//            return gpsArray;
//        }
//    }

    public static void addFriend(String name, String number) {
        friendList.add(new Friend(name, number));
        friendList_Text.setText(friendList_Text.getText() + "\n" + name + " : " + number);
    }

    public void addFriendMenu(View view) {
        Intent intent = new Intent(this, AddFriendMenu.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getNumber() {
        TelephonyManager myNumber = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return myNumber.getLine1Number();
    }

    public void myFancyMethod(View v) {
        String phoneNo = "3147577588";
        String message = "FUCK!!!";
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, message, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
    }

}