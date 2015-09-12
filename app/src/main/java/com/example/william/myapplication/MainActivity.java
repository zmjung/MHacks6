package com.example.william.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static ArrayList<Friend> friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        friendList = new ArrayList<>();
        dataBase();
    }

    public void dataBase() {
        Firebase myFirebaseRef = new Firebase("https://dazzling-heat-5469.firebaseio.com/");
        final String phoneNumber = "510-364-9006";
        final int[] gps = new int[] {100, 100};
        Map<String, int[]> map = new HashMap<>();
        map.put(phoneNumber, gps);
        myFirebaseRef.child(phoneNumber).setValue(map.get(phoneNumber));
        myFirebaseRef.child(phoneNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TextView t = (TextView) findViewById(R.id.test);
                t.setText(snapshot.getValue().toString());
                var data = snapshot.val();
                // data equals { "name": { "first": "Fred", "last": "Flintstone" }, "age": 53 }
                console.log(data.name.first);  // "Fred"
                console.log(data.age);  // 53
                Map<String, int[]> returnMap = new HashMap<>();
                returnMap.put()
//                for (String s : phoneNumber) {
//                    if (s.equals(value) {
//
//                    }
//
//                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public static void addFriend(String name, String number) {
        friendList.add(new Friend(name, number));
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