package com.example.coctracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Timer {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long accountId;
    public String name;
    public long endTime; // Stored as milliseconds since epoch
    public boolean isBuilderBase;
    public String category; // New field for categorization
}
