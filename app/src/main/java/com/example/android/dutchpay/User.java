package com.example.android.dutchpay;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class User {

    public String username;
    public String uid;
    public String email;
    public String password;
    public int balance;
    public List<String> friendList;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String uid, String email, String password, int balance, List<String> friendList) {
        this.username = username;
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.balance = balance;
        this.friendList = friendList;
    }

}
