package com.androidhive.androidsqlite;

import com.example.william.myapplication.Friend;

public class Database {

    //private variables
    Friend friend;

    // Empty constructor
    public Database(){

    }
    // constructor
    public Database(Friend friend){
        this.friend = friend;
    }

    public Friend getFriend() {
        return this.friend;
    }

    // setting id
    public void setFriend(Friend friend){
        this.friend = friend;
    }

}
