package com.example.william.myapplication;

/**
 * Created by Zhijian on 9/12/2015.
 */
public class Friend {
    private String number;
    private String name;

    public Friend(String name, String number) {
        this.number = number;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }
}
