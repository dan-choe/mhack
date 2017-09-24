package com.example.android.dutchpay;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class User {
    private String username;
    private String uid;
    private String email;
    private String password;
    private double balance;
    private List<String> friendList;
    private double change;
    private String changeBy;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        friendList = new ArrayList<>();
    }

    public double getChange() {
        return change;
    }

    public User(String username, String uid, String email, String password, int balance, List<String> friendList) {
        this.username = username;
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.balance = balance;
        this.friendList = friendList;
        this.change = 0;
        this.changeBy = "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<String> friendList) {
        this.friendList = friendList;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getChangeBy() {
        return changeBy;
    }

    public void setChangeBy(String changeBy) {
        this.changeBy = changeBy;
    }
}
