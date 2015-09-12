package com.example.william.myapplication;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static ArrayList<Friend> friendList;
    private static TextView friendList_Text;
    protected static final String TAG = "basic-location-sample";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        friendList = new ArrayList<>();
        friendList_Text = (TextView) findViewById(R.id.test);
        dataBase();


        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

        buildGoogleApiClient();
        friendList.add(new Friend("William Hsu", "5103649006"));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
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


    public void dataBase() {
        Firebase myFirebaseRef = new Firebase("https://dazzling-heat-5469.firebaseio.com/");
        final String phoneNumber = "5103649006";
        final ArrayList<Long> gps = new ArrayList<>();
        if (mLastLocation != null) {
            gps.add((long) mLastLocation.getLongitude());
            gps.add((long) mLastLocation.getLatitude());
        } else {
            gps.add((long) 100);
            gps.add((long) 100);
        }
        Map<String, ArrayList<Long>> map = new HashMap<>();
        map.put(phoneNumber, gps);
        myFirebaseRef.child("PhoneNumbers").setValue(map);
        myFirebaseRef.child("PhoneNumbers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TextView t = (TextView) findViewById(R.id.test);
                t.setText(snapshot.getValue().toString());
                Map<String, ArrayList<Long>> data = (HashMap) snapshot.getValue();
                System.out.println("There are " + snapshot.getChildrenCount() + " phone numbers");
                ArrayList<String> friends = new ArrayList<>();
                for (Friend s : friendList) {
                   // int[] location = data.get(s.getNumber());
                   // System.out.println(location);
                    if ((Math.abs(data.get(s.getNumber()).get(0) - gps.get(0)) < 1 ) && Math.abs(data.get(s.getNumber()).get(1) - gps.get(1)) < 1 ) {
                        friends.add(s.getName());
                    }
                }
                String original = "";
                for (String a : friends) {
                    original = "\n" + original + a + " is this working";
                    t.setText(original);
                }
                // ...
//                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
//                    GPS post = postSnapshot.getValue(GPS.class);
//                    System.out.println(post.getGps());

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



}