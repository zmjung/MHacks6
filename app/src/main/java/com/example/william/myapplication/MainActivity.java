package com.example.william.myapplication;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static ArrayList<Friend> friendList;
    protected static final String TAG = "location-updates-sample";
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private static Context context;
    private static Firebase myFirebaseRef;
    private HashSet<String> friends = new HashSet<>();


    /**
     * Represents a geographical location.
     */
    protected double curLatitude;
    protected double curLongitude;

    protected GoogleApiClient mGoogleApiClient;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    protected Boolean mRequestingLocationUpdates;

    protected String mLastUpdateTime;

    private static String deviceNumber;
    private TextView friendsListView;
    private String deviceFriends = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getNumber();
        Firebase.setAndroidContext(this);
        context = getApplicationContext();
        friendList = new ArrayList<>();
        myFirebaseRef = new Firebase("https://dazzling-heat-5469.firebaseio.com/");
        myFirebaseRef.child("FriendsList").child(deviceNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                friendList = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    friendList.add((new Friend((String) postSnapshot.getValue(), postSnapshot.getKey())));
                    System.out.println(postSnapshot.getValue() + " : " + postSnapshot.getKey());
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


        dataBase();



        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();

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
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
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
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        curLongitude = mCurrentLocation.getLongitude();
        curLatitude = mCurrentLocation.getLatitude();
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
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
        startLocationUpdates();
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
        Firebase curNumber = myFirebaseRef.child("PhoneNumbers").child(deviceNumber);
        curNumber.child("0").setValue(mCurrentLocation.getLatitude());
        curNumber.child("1").setValue(mCurrentLocation.getLongitude());
//        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }





    private void dataBase() {
        myFirebaseRef.child("PhoneNumbers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double lat;
                double lon;
                for (Friend s : friendList) {
                    if (((HashMap) snapshot.getValue()).containsKey(s.getNumber())) {
                        lat = (double) snapshot.child(s.getNumber()).child("0").getValue();
                        lon = (double) snapshot.child(s.getNumber()).child("1").getValue();
                        if (Math.abs(lat - curLatitude) < .00005 && Math.abs(lon - curLongitude) < .00005) {
//                            ((TextView) findViewById(R.id.friendsTest)).setText(lat + " : " + lon
//                                    + " | " + curLatitude + " : " + curLongitude);

                            friends.add(s.getNumber());

//                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
//                            mBuilder.setContentTitle("Notification Alert, Click Me!");
//                            mBuilder.setContentText("Hi, This is Android Notification Detail!");
//                            int notificationID = 001;
//                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                            // notificationID allows you to update the notification later on.
//                            mNotificationManager.notify(notificationID, mBuilder.build());

                            Notify(s.getName(), s.getNumber());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }
    @TargetApi(21)
    private void Notify(String name, String number){
//        Notification notification  = new Notification.Builder(this)
//                .setCategory(Notification.CATEGORY_MESSAGE)
//                .setContentTitle()
//                .setContentText("Open Slapchat and SLAP THEM!")
//                .setSmallIcon(R.drawable.mr_ic_audio_vol)
//                .setAutoCancel(true)
//                .setVisibility(1).build();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mr_ic_audio_vol)
                        .setContentTitle(name + " is nearby!")
                        .setContentText("Open Slapchat and SLAP THEM!");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(001, mBuilder.build());
    }
    public static void addFriend(String name, String number) {
        myFirebaseRef.child("FriendsList").child(deviceNumber).child(number).setValue(name);
        friendList.add(new Friend(name, 1 + number));

    }
    public static void removeFriend(String name, String number) {
        myFirebaseRef.child("FriendsList").child(deviceNumber).child(number).removeValue();
        for (Friend f : friendList) {
            if (f.getName().equals(name) && f.getNumber().equals(number)) {
                friendList.remove(f);
            }
        }
    }

    public void removeFriendMenu(View view) {
        Intent intent = new Intent(this, RemoveFriend.class);
        startActivity(intent);
    }

    public void addFriendMenu(View view) {
        Intent intent = new Intent(this, AddFriendMenu.class);
        startActivity(intent);
    }

    public void friendsListMenu(View view) {
        Intent intent = new Intent(this, FriendsList.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getNumber() {
        TelephonyManager myNumber = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceNumber = myNumber.getLine1Number();
        if (deviceNumber == null) deviceNumber = "9782014798";
    }

    private void myFancyMethod(View v) {
        String message = "SLAP!!!";
        SmsManager sms = SmsManager.getDefault();

        if (friends == null || friends.size() == 0) {
            sms.sendTextMessage(deviceNumber, null, message, null, null);
        } else {
            for (String elem : friends) {
                sms.sendTextMessage(elem, null, message, null, null);
            }
        }
        Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
    }

    public void about(View v) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }
}