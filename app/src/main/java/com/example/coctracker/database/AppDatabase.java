package com.example.coctracker.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.coctracker.models.Account;
import com.example.coctracker.models.History;
import com.example.coctracker.models.Timer;

// Bump the version number to 5
@Database(entities = {Account.class, Timer.class, History.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AccountDao accountDao();
    public abstract TimerDao timerDao();
    public abstract HistoryDao historyDao();
}
