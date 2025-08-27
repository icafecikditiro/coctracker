package com.example.coctracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Account {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String playerTag;
    public int townHallLevel;
}
