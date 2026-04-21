package com.osaid.learningassistant.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String email;
    public String password;
    public String phone;
    public String interests;

    public User(String username, String email, String password, String phone, String interests) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.interests = interests;
    }
}