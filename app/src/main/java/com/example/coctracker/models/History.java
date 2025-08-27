package com.example.coctracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long accountId;
    public String upgradeName;
    public long completionDate; // Stored as milliseconds
}
    