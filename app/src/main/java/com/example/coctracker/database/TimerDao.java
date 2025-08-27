package com.example.coctracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.coctracker.models.Timer;
import java.util.List;

@Dao
public interface TimerDao {
    @Query("SELECT * FROM timer WHERE accountId = :accountId")
    List<Timer> getTimersForAccount(long accountId);

    @Insert
    long insert(Timer timer);

    @Delete
    void delete(Timer timer);

    @Query("SELECT * FROM timer WHERE id = :id")
    Timer getTimerById(long id);

    @Query("DELETE FROM timer WHERE accountId = :accountId")
    void deleteAllByAccountId(long accountId);
}
